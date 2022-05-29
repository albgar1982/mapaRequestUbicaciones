package com.example.maparequestubicaciones

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.maparequestubicaciones.databinding.ActivityPruebaarBinding
import com.google.android.gms.maps.model.LatLng
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.*

class PruebaARActivity : AppCompatActivity() {

    companion object {
        const val TAG_TOKEN = "TAG_TOKEN"
        const val TAG_USER = "TAG_USER"
        const val TAG_RUTA = "TAG_RUTA"

        fun launch(context: Context, ruta: String, user: String, token: String) {
            val intent = Intent(context, PruebaARActivity::class.java)
            intent.putExtra(TAG_TOKEN, token)
            intent.putExtra(TAG_USER, user)
            intent.putExtra(TAG_RUTA, ruta)
            context.startActivity(intent)
        }
    }

    private lateinit var arFragment: ArFragment
    private val modelLink = "https://github.com/yudiz-solutions/runtime_ar_android/raw/master/model/model.gltf"
    private lateinit var renderable: ModelRenderable
    private lateinit var binding: ActivityPruebaarBinding
    private var usuario = ""
    private var ruta = ""
    private var token = ""
    private val viewModel: PruebaARViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPruebaarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usuario = intent.getStringExtra(TAG_USER).toString()
        ruta = intent.getStringExtra(TAG_RUTA).toString()
        token = intent.getStringExtra(TAG_TOKEN).toString()

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

                return@exceptionally null
            }


        var yaGuardado=false
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


            binding.back.visibility= View.VISIBLE

            println("Voy a intentar guardar. yaGuardado está: $yaGuardado")
            if(!yaGuardado) {
                viewModel.salvarProgreso(usuario, ruta,this)
                yaGuardado = true
               // MainActivity.launch(this,ruta,usuario,token)
            }


            println("Está guardado = $yaGuardado")

        }

        binding.back.setOnClickListener {
            MainActivity.launch(this,ruta,usuario,token)
        }

    }

}