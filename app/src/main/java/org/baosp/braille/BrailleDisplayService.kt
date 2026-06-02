package org.baosp.braille

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.io.IOException
import java.io.OutputStream
import java.util.UUID
import java.util.concurrent.LinkedBlockingQueue

/**
 * BrailleDisplayService — AccessibilityService that routes screen content
 * to a connected Bluetooth HID Braille display.
 *
 * Lifecycle:
 *  1. onServiceConnected — configure which events to capture
 *  2. onAccessibilityEvent — receive focus/text events from the OS
 *  3. sendToBrailleDisplay — translate text and write to Bluetooth socket
 */
class BrailleDisplayService : AccessibilityService() {

    private val tag = "BaospBraille"

    // Bluetooth connection
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var connectionThread: Thread? = null
    private var sendThread: Thread? = null
    private val sendQueue = LinkedBlockingQueue<ByteArray>(64)

    // Last text sent — avoid repeating the same output
    private var lastSentText = ""

    // Grade selection: false = Grade 1, true = Grade 2
    private var useGrade2 = false

    companion object {
        private val SERIAL_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        /**
         * Partial names of known Braille displays for auto-detection.
         * Add entries here when supporting a new device model.
         */
        val knownDisplayNames = listOf(
            "orbit reader",
            "refreshabraille",
            "focus blue",
            "braillenote",
            "brailliant",
            "vario",
            "humanware",
            "freedom scientific",
            "hims",
            "optelec",
            "esys",
            "eurobraille",
            "papenmeier",
            "handytech"
        )
    }

    // ──────────────────────────────────────────────────────────────────────
    // AccessibilityService lifecycle
    // ──────────────────────────────────────────────────────────────────────

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(tag, "BAOSP Braille service connected")

        val info = AccessibilityServiceInfo().apply {
            eventTypes = (
                AccessibilityEvent.TYPE_VIEW_FOCUSED or
                AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED or
                AccessibilityEvent.TYPE_VIEW_CLICKED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_ANNOUNCEMENT
            )
            feedbackType = AccessibilityServiceInfo.FEEDBACK_BRAILLE
            flags = (
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            )
            notificationTimeout = 100
        }
        serviceInfo = info

        startSendThread()
        connectToDisplay()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        val text = extractText(event)
        if (text.isNullOrBlank() || text == lastSentText) return
        lastSentText = text

        val brailleBytes = if (useGrade2) {
            BrailleTranslator.toGrade2Bytes(text)
        } else {
            BrailleTranslator.toGrade1Bytes(text)
        }

        sendQueue.offer(brailleBytes)
    }

    override fun onInterrupt() {
        Log.w(tag, "BAOSP Braille service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionThread?.interrupt()
        sendThread?.interrupt()
        try { bluetoothSocket?.close() } catch (_: IOException) {}
        Log.i(tag, "BAOSP Braille service destroyed")
    }

    // ──────────────────────────────────────────────────────────────────────
    // Text extraction from AccessibilityEvents
    // ──────────────────────────────────────────────────────────────────────

    private fun extractText(event: AccessibilityEvent): String? {
        // Try contentDescription first (most reliable for buttons, icons)
        val source: AccessibilityNodeInfo? = event.source
        if (source != null) {
            val desc = source.contentDescription?.toString()
            if (!desc.isNullOrBlank()) {
                source.recycle()
                return desc
            }
            val nodeText = source.text?.toString()
            if (!nodeText.isNullOrBlank()) {
                source.recycle()
                return nodeText
            }
            source.recycle()
        }

        // Fall back to event text
        val eventText = event.text.joinToString(" ")
        if (eventText.isNotBlank()) return eventText

        return event.contentDescription?.toString()
    }

    // ──────────────────────────────────────────────────────────────────────
    // Bluetooth connection
    // ──────────────────────────────────────────────────────────────────────

    private fun connectToDisplay() {
        connectionThread = Thread {
            try {
                val bm = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
                val adapter: BluetoothAdapter = bm.adapter ?: run {
                    Log.w(tag, "No Bluetooth adapter")
                    return@Thread
                }

                val display = findPairedDisplay(adapter) ?: run {
                    Log.i(tag, "No paired Braille display found")
                    return@Thread
                }

                val displayName = try { display.name } catch (_: SecurityException) { "display" }
                Log.i(tag, "Connecting to $displayName …")

                val socket = display.createRfcommSocketToServiceRecord(SERIAL_UUID)
                socket.connect()
                bluetoothSocket = socket
                outputStream = socket.outputStream
                Log.i(tag, "Connected to $displayName")

            } catch (e: SecurityException) {
                Log.e(tag, "Bluetooth permission denied: ${e.message}")
            } catch (e: IOException) {
                Log.e(tag, "Bluetooth connect failed: ${e.message}")
            }
        }.also { it.isDaemon = true; it.start() }
    }

    private fun findPairedDisplay(adapter: BluetoothAdapter): BluetoothDevice? {
        val bonded: Set<BluetoothDevice> = try {
            adapter.bondedDevices
        } catch (_: SecurityException) {
            return null
        }

        return bonded.firstOrNull { device ->
            val name = try { device.name?.lowercase() } catch (_: SecurityException) { null }
            name != null && knownDisplayNames.any { name.contains(it) }
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Send thread — drains the queue and writes to Bluetooth
    // ──────────────────────────────────────────────────────────────────────

    private fun startSendThread() {
        sendThread = Thread {
            while (!Thread.interrupted()) {
                try {
                    val bytes = sendQueue.take()
                    val out = outputStream
                    if (out != null) {
                        out.write(bytes)
                        out.flush()
                    }
                } catch (_: InterruptedException) {
                    break
                } catch (e: IOException) {
                    Log.e(tag, "Send failed: ${e.message}")
                    outputStream = null
                    bluetoothSocket = null
                }
            }
        }.also { it.isDaemon = true; it.start() }
    }
}
