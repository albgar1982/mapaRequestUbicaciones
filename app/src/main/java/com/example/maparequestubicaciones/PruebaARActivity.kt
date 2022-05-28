package com.example.maparequestubicaciones

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.maparequestubicaciones.databinding.ActivityPruebaarBinding
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class PruebaARActivity : AppCompatActivity() {

    companion object {

        const val TAG_USER = "TAG_USER"
        const val TAG_RUTA = "TAG_RUTA"

        fun launch(context: Context, ruta: String, user: String) {
            val intent = Intent(context, PruebaARActivity::class.java)
            intent.putExtra(TAG_USER, user)
            intent.putExtra(TAG_RUTA, ruta)
            context.startActivity(intent)
        }
    }

    private lateinit var arFragment: ArFragment
    private val modelLink = "https://github.com/yudiz-solutions/runtime_ar_android/raw/master/model/model.gltf"
    private lateinit var renderable: ModelRenderable
    private lateinit var binding: ActivityPruebaarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPruebaarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment

        arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            arFragment.onUpdate(frameTime)
        }

        // Build renderable from the link
        ModelRenderable.builder()
            .setSource(this, RenderableSource.builder().setSource(
                this,
                Uri.parse(modelLink),
                RenderableSource.SourceType.GLTF2).build())
            .setRegistryId(modelLink)
            .build()
            .thenAccept { renderable = it }
            .exceptionally {
                Toast.makeText(this@PruebaARActivity, "Error in fetching $modelLink", Toast.LENGTH_LONG).show()
                return@exceptionally null
            }
        var cont=0
        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->


            // Create the Anchor.
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            // Create a transformable node and add it to the anchor.
            val node = TransformableNode(arFragment.transformationSystem)
            node.setParent(anchorNode)
            node.renderable = renderable
            node.select()


            cont++
            println(cont)
        }

        binding.back.setOnClickListener {
         //   MainActivity.launch(this,"","","")
        }

    }
}