package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.GeoPin

class AndroidMapBridge(
    private val onLocationSelected: (Double, Double) -> Unit,
    private val onAskAboutLocation: (Double, Double) -> Unit,
    private val onPoiSelected: (String, String, Double, Double) -> Unit
) {
    @JavascriptInterface
    fun onLocationSelected(lat: Double, lng: Double) {
        onLocationSelected.invoke(lat, lng)
    }

    @JavascriptInterface
    fun onAskAboutLocation(lat: Double, lng: Double) {
        onAskAboutLocation.invoke(lat, lng)
    }

    @JavascriptInterface
    fun onPoiSelected(id: String, title: String, lat: Double, lng: Double) {
        onPoiSelected.invoke(id, title, lat, lng)
    }

    @JavascriptInterface
    fun onMapMoved(lat: Double, lng: Double, zoom: Int) {
        // Can be used for dynamic viewport tracking
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapWebView(
    modifier: Modifier = Modifier,
    currentLat: Double,
    currentLng: Double,
    activePins: List<GeoPin>,
    pendingFlyTo: Pair<Double, Double>?,
    onLocationSelected: (Double, Double) -> Unit,
    onAskAboutLocation: (Double, Double) -> Unit,
    onPoiSelected: (GeoPin) -> Unit,
    onPendingFlyToConsumed: () -> Unit
) {
    val context = LocalContext.current

    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.evaluateJavascript("initMap($currentLat, $currentLng, 14, true);", null)
                }
            }

            addJavascriptInterface(
                AndroidMapBridge(
                    onLocationSelected = { lat, lng -> onLocationSelected(lat, lng) },
                    onAskAboutLocation = { lat, lng -> onAskAboutLocation(lat, lng) },
                    onPoiSelected = { id, title, lat, lng ->
                        val pin = activePins.find { it.id == id } ?: GeoPin(id, title, "", lat, lng)
                        onPoiSelected(pin)
                    }
                ),
                "AndroidBridge"
            )

            loadUrl("file:///android_asset/map.html")
        }
    }

    // React to pending flyTo
    LaunchedEffect(pendingFlyTo) {
        if (pendingFlyTo != null) {
            val (lat, lng) = pendingFlyTo
            webView.evaluateJavascript("flyToLocation($lat, $lng, 15);", null)
            onPendingFlyToConsumed()
        }
    }

    // React to active pins update
    LaunchedEffect(activePins) {
        webView.evaluateJavascript("clearPoiMarkers();", null)
        for (pin in activePins) {
            val safeTitle = pin.title.replace("'", "\\'")
            val safeDesc = pin.description.replace("'", "\\'")
            val safeCat = pin.category.replace("'", "\\'")
            val js = "addPoiMarker('${pin.id}', '$safeTitle', '$safeDesc', ${pin.lat}, ${pin.lng}, '$safeCat');"
            webView.evaluateJavascript(js, null)
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}
