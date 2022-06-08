package com.example.maparequestubicaciones

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.example.maparequestubicaciones.databinding.ActivityPruebaarBinding
import kotlin.random.Random


class ARActivity : AppCompatActivity() {

    companion object {
        const val TAG_TOKEN = "TAG_TOKEN"
        const val TAG_USER = "TAG_USER"
        const val TAG_RUTA = "TAG_RUTA"
        const val TAG_LLAVE = "TAG_LLAVE"


        fun launch(context: Context, ruta: String, user: String, token: String,llave:Int) {
            val intent = Intent(context, ARActivity::class.java)
            intent.putExtra(TAG_TOKEN, token)
            intent.putExtra(TAG_USER, user)
            intent.putExtra(TAG_RUTA, ruta)
            intent.putExtra(TAG_LLAVE, llave)

            context.startActivity(intent)
        }
    }

    private lateinit var arFragment: ArFragment

    //Papá Noel
    // private val modelLink = https://github.com/yudiz-solutions/runtime_ar_android/raw/master/model/model.gltf
    //Regalo
    //https://githubraw.com/albgar1982/regalo/main/scene.gltf
    //Regalo 2
    //https://githubraw.com/albgar1982/regalo1/main/scene.gltf
    //Casa de jengibre
    //https://githubraw.com/albgar1982/ginger/main/scene.gltf
    //Christmas
    //https://githubraw.com/albgar1982/christmas/main/scene.gltf
    //Christmas2
    //https://githubraw.com/albgar1982/christmas2/main/scene.gltf
    private var renderable: ModelRenderable? = null
    private lateinit var binding: ActivityPruebaarBinding
    private var usuario = ""
    private var ruta = ""
    private var token = ""
    private var llave=0
    private val viewModel: ARViewModel by viewModels()
    private val objetos3d = listOf("https://github.com/yudiz-solutions/runtime_ar_android/raw/master/model/model.gltf","https://githubraw.com/albgar1982/regalo/main/scene.gltf","https://githubraw.com/albgar1982/regalo1/main/scene.gltf"
    ,"https://githubraw.com/albgar1982/christmas/main/scene.gltf","https://githubraw.com/albgar1982/christmas2/main/scene.gltf","https://githubraw.com/albgar1982/ginger/main/scene.gltf")
    private val aleat = Random.nextInt(objetos3d.size)
    private val objeto = objetos3d[aleat]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPruebaarBinding.inflate(layoutInflater)
        setContentView(binding.root)


        usuario = intent.getStringExtra(TAG_USER).toString()
        ruta = intent.getStringExtra(TAG_RUTA).toString()
        token = intent.getStringExtra(TAG_TOKEN).toString()
        llave = intent.getIntExtra(TAG_LLAVE,0)

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment

        arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            arFragment.onUpdate(frameTime)
        }

        // Build renderable from the link
        ModelRenderable.builder()
            .setSource(
                this, RenderableSource.builder().setSource(
                    this,
                    Uri.parse(objeto),
                    RenderableSource.SourceType.GLTF2
                ).build()
            )
            .setRegistryId(objeto)
            .build()
            .thenAccept { modelRenderable ->
                renderable = modelRenderable
                Toast.makeText(this,"Puede colocar el regalo.",Toast.LENGTH_LONG).show()
            }
            .exceptionally {
                Toast.makeText(this,"Descarga FALLIDA, compruebe su red.",Toast.LENGTH_LONG).show()
                return@exceptionally null
            }


        var yaGuardado = false
        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            if (renderable == null)
                return@setOnTapArPlaneListener

            // Create the Anchor.
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)


            // Create a transformable node and add it to the anchor.
            val node = TransformableNode(arFragment.transformationSystem)
            node.renderable = renderable
            if(aleat == 1 || aleat == 2){
                node.scaleController.minScale = 0.22f
                node.scaleController.maxScale = 0.23f
            }
            else{
                node.scaleController.minScale = 0.27f
                node.scaleController.maxScale = 0.28f
            }

            node.worldScale = Vector3(0.4f,0.4f,0.4f)
            node.setParent(anchorNode)
            node.select()

            if (!yaGuardado) {
                viewModel.salvarProgreso(usuario, ruta, this)
                yaGuardado = true
                binding.laySigui.visibility = View.VISIBLE
            }
            println("Está guardado = $yaGuardado")
        }

        binding.laySigui.setOnClickListener {
            MainActivity.launch(this, ruta, usuario, token)
        }
    }
}
