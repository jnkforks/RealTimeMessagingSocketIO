package com.polatechno.realtimemessagingsocetio.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.polatechno.realtimemessagingsocetio.data.model.MessageItem
import com.polatechno.realtimemessagingsocetio.R
import com.polatechno.realtimemessagingsocetio.util.LOG_TAG_MAIN
import com.polatechno.realtimemessagingsocetio.util.MB_URL
import com.polatechno.realtimemessagingsocetio.util.TEST_ROOM_ID
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var isDisconnect = false
    private lateinit var socket: Socket
    private val messageList = ArrayList<MessageItem>()
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initSocketIOConnection()
        initRecyclerView()
        setOnClicks()
    }

    private fun initSocketIOConnection() {
        val opts = IO.Options()
        opts.transports = arrayOf("websocket")

        try {
            socket = IO.socket(MB_URL, opts)
            socket.on(Socket.EVENT_CONNECT) {
                runOnUiThread {
                    joinToRoom()
                    showToast("Connected")
                    btnConnect.text = "Disconnect"

                }
                Log.d(LOG_TAG_MAIN, "SocketEventConnect:  " + it.toString())

            }

            socket.connect()
        } catch (ex: Exception) {
            showToast("Error: " + ex.message)
        }
    }

    private fun setOnClicks() {
        btnConnect!!.setOnClickListener(this)
        btnJoinRoom!!.setOnClickListener(this)
        btnLeaveRoom!!.setOnClickListener(this)
        btnAddItem!!.setOnClickListener(this)
    }

    private fun initRecyclerView() {
        messageAdapter =
            MessageAdapter(
                messageList
            )
        recycler_view.adapter = messageAdapter
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)
    }

    private fun joinToRoom() {
        Log.d(LOG_TAG_MAIN, "Trying to join the Room")

        if(socket.connected()){
            socket.emit("subscribe", TEST_ROOM_ID, Ack {
                runOnUiThread {
                    // ack from client to server
                    Log.d(LOG_TAG_MAIN, "Join the room status: " + it[0])
                    showToast("Room connect status: " + it[0])
                    val result = it.get(0) as JSONObject
                    val status: String

                    try {
                        status = result.getString("status")
                    } catch (e: JSONException) {
                        Log.d(LOG_TAG_MAIN, e.message)
                        return@runOnUiThread
                    }

                    if (status.equals("ok")) {
                        listenIncomingMessages()
                    } else {
                        showToast("Could not connect to room!")
                    }
                }
            })
        }else{
            showToast("Not connected to socketio server. Please connect first, then try again...")
        }



    }

    private fun listenIncomingMessages() {

        socket.on(TEST_ROOM_ID, Emitter.Listener {
            Log.d(LOG_TAG_MAIN, "Incoming message: " + it[0])


            val data = it.get(0) as JSONObject
            val username: String;
            val message: String;
            val type: String;
            val userId: String

            try {
                type = data.getString("type")
                username = data.getString("username")
                userId = data.getString("userId")
                message = data.getString("message")


            } catch (e: JSONException) {
                Log.d(LOG_TAG_MAIN, e.message)
                return@Listener
            }
            val item =
                MessageItem(
                    type,
                    username,
                    userId,
                    message
                )
            pushNewMessage(item)
        })
    }

    private fun pushNewMessage(newMessage: MessageItem) {

        messageAdapter.addItem(newMessage)
        recycler_view.smoothScrollToPosition(messageAdapter.itemCount - 1)

    }

    private fun leaveTheRoom() {
        socket.emit("unsubscribe", TEST_ROOM_ID, Ack {
            runOnUiThread {
                Log.d(LOG_TAG_MAIN, "Leave the room status: " + it[0])

                showToast("Leaving the room status: " + it[0])
                val result = it.get(0) as JSONObject
                val status: String

                try {
                    status = result.getString("status")
                } catch (e: JSONException) {
                    Log.d(LOG_TAG_MAIN, e.message)
                    return@runOnUiThread
                }

                if (status.equals("ok")) {
                    showToast("Unsubscribed. Left the room!")
                } else {
                    showToast("Could not unsubscribe!")
                }
            }
        })
    }

    private fun showToast(message: String) {

        Toast.makeText(
            this@MainActivity,
            message,
            Toast.LENGTH_SHORT
        ).show()

    }

    override fun onClick(p0: View?) {
        val item_id = p0?.id

        when (item_id) {
            R.id.btnConnect -> handleConnectButton()
            R.id.btnJoinRoom -> joinToRoom()
            R.id.btnLeaveRoom -> leaveTheRoom()
            R.id.btnAddItem -> addTestMessage()
        }
    }

    private fun handleConnectButton() {
        if (!isDisconnect) {
            socket.disconnect()
            btnConnect.text = "Connect"
        } else {
            socket.connect()
            btnConnect.text = "Disconnect"
        }
        isDisconnect = !isDisconnect
    }

    private fun addTestMessage() {

        val item =
            MessageItem(
                "ChatMessagew",
                "polatechno",
                "testingUserId",
                "Hello"
            )
        pushNewMessage(item)

    }
}