package com.app.arcoredemo

import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var arFragment: ArFragment
    private var modelRenderable: ModelRenderable? = null

    private val GLTF_ASSET =
    "https://github.com/ajiqoyum/ProjectGKom/blob/master/android.glb?raw=true"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = fragment as ArFragment
        setUpNetworkModel()
        setUpPlane()

        /**
         * Touch listener to detect when a user touches the ArScene plane to place a model
         */
//        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
//            setModelOnUi(hitResult)
//        }
    }

private fun setUpNetworkModel() {
    ModelRenderable.builder()
        .setSource(
            this, RenderableSource.builder().setSource(
                this,
                Uri.parse(GLTF_ASSET),
                RenderableSource.SourceType.GLB
            )
                .setScale(0.5f)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build()
        )
        .setRegistryId(GLTF_ASSET)
        .build()
        .thenAccept { renderable: ModelRenderable -> modelRenderable = renderable }
}
    /**
     * Used to load model and set it on ArScene where a user Taps
     */
    private fun setModelOnUi(hitResult: HitResult) {
        loadModel(R.raw.andy) { modelRenderable ->
            //Used to get anchor point on scene where user tapped
            val anchor = hitResult.createAnchor()
            //Created an anchor node to attach the anchor with its parent
            val anchorNode = AnchorNode(anchor)
            //Added arSceneView as parent to the anchorNode. So our anchors will bind to arSceneView.
            anchorNode.setParent(arFragment.arSceneView.scene)

            //TransformableNode for out model. So that it can be rotated, scaled etc using gestures
            val transformableNode = TransformableNode(arFragment.transformationSystem)
            //Assigned anchorNode as parent so that our model stays at the position where user taps
            transformableNode.setParent(anchorNode)
            //Assigned the resulted model received from loadModel method to transformableNode
            transformableNode.renderable = modelRenderable
            //Sets this node as selected node by default
            transformableNode.select()
        }
    }

    private fun createModel(anchorNode: AnchorNode) {
        val node = TransformableNode(arFragment!!.transformationSystem)
        node.setParent(anchorNode)
        node.renderable = modelRenderable
        node.select()
    }

    private fun setUpPlane() {
        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment!!.arSceneView.scene)
            createModel(anchorNode)
        }
    }

    /**
     * Used to laod models from 'raw' with a callback when loading is complete
     */
    fun loadModel(@RawRes model: Int, callback: (ModelRenderable) -> Unit) {
        ModelRenderable
            .builder()
            .setSource(this, model)
            .build()
            .thenAccept { modelRenderable ->
                callback(modelRenderable)
            }
    }
}