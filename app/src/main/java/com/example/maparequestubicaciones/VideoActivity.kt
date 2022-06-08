package com.example.maparequestubicaciones

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.maparequestubicaciones.databinding.ActivityVideoBinding

class VideoActivity : AppCompatActivity() {
    companion object {
        const val TAG_TOKEN = "TAG_TOKEN"
        const val TAG_USER = "TAG_USER"

        fun launch(context: Context, user:String, token: String) {
            val intent = Intent(context, VideoActivity::class.java)
            intent.putExtra(TAG_TOKEN, token)
            intent.putExtra(TAG_USER, user)
            context.startActivity(intent)
        }
    }

    private lateinit var binding : ActivityVideoBinding
    // declaring a null variable for VideoView
    var simpleVideoView: VideoView? = null

    // declaring a null variable for MediaController
    var mediaControls: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val token = intent.getStringExtra(TAG_TOKEN).toString()
        val user = intent.getStringExtra(TAG_USER).toString()

        // assigning id of VideoView from
        // activity_main.xml layout file
        simpleVideoView = binding.videoView

        if (mediaControls == null) {
            // creating an object of media controller class
            mediaControls = MediaController(this)

            // set the anchor view for the video view
            mediaControls!!.setAnchorView(this.simpleVideoView)
        }
        // set the media controller for video view
        simpleVideoView!!.setMediaController(mediaControls)

        // set the absolute path of the video file which is going to be played
        simpleVideoView!!.setVideoURI(
            Uri.parse(
                "android.resource://" + packageName +"/"+R.raw.santa
            )
        )

        simpleVideoView!!.requestFocus()

        // starting the video
        simpleVideoView!!.start()

        // after the video is completed
        simpleVideoView!!.setOnCompletionListener {
            binding.button.visibility = View.VISIBLE
        }

        binding.button.setOnClickListener {
            SeleccionRutaActivity.launch(this,user,token)
        }

        // display a toast message if any
        // error occurs while playing the video
        simpleVideoView!!.setOnErrorListener { mp, what, extra ->
            Toast.makeText(
                applicationContext, "An Error Occured " +
                        "While Playing Video !!!", Toast.LENGTH_LONG
            ).show()
            false
        }
    }
}