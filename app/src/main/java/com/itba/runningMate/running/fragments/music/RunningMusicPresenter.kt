package com.itba.runningMate.running.fragments.music

import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import timber.log.Timber
import java.lang.ref.WeakReference

class RunningMusicPresenter(view: RunningMusicView?) {

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private val REQUEST_CODE = 1138
    private val REDIRECT_URI = "runningmate://running"
    private val CLIENT_ID = "09b7faf352454d0a86db9ea9e5e2a43f" //CLIENT_ID de local.properties

    private val view: WeakReference<RunningMusicView> = WeakReference(view)

    fun onPlayButtonClick(locked: Boolean) {
        if (view.get() == null) {
            return
        }
        if(spotifyAppRemote == null){
            loginSpotify()
        }
        spotifyAppRemote?.playerApi?.resume()
        if (!locked) {
            view.get()!!.changeButton(true)
        }
    }

    fun onPauseButtonClick() {
        if(spotifyAppRemote == null){
            loginSpotify()
        }
        spotifyAppRemote?.playerApi?.pause()
        if (view.get() == null) {
            return
        }
        view.get()!!.changeButton(false)
    }

    fun onNextButtonClick() {
        if(spotifyAppRemote == null){
            loginSpotify()
        }
        spotifyAppRemote?.playerApi?.skipNext()
    }

    fun onBackButtonClick() {
        if(spotifyAppRemote == null){
            loginSpotify()
        }
        spotifyAppRemote?.playerApi?.skipPrevious()
    }

    fun onViewAttached() {
        if (view.get() == null) {
            return
        }
        view.get()!!.connectSpotify()
    }

    fun onViewDetached() {
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }

    fun getSpotifyConnectParams(): ConnectionParams? {
        return ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()
    }

    fun onSpotifyConnectFailure() {
        Timber.d("Could not connect to Spotify")
        loginSpotify()
    }

    private fun loginSpotify() {
        if (view.get() == null) {
            return
        }

        val builder = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            REDIRECT_URI
        )
        builder.setShowDialog(true)
        //builder.setScopes(arrayOf("streaming"))
        val request = builder.build()
        view.get()!!.openLoginActivity(REQUEST_CODE,request)
    }

    fun spotifyConnected(spotifyAppRemote: SpotifyAppRemote?) {
        this.spotifyAppRemote = spotifyAppRemote
        this.spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback {
            var name = "No song selected"
            if (it.track?.name != null && it.track?.artist?.name != null)
                name = it.track?.name + " - " + it.track?.artist?.name
            view.get()!!.setSongName(name)
            if(it?.track == null) {
                this.spotifyAppRemote?.playerApi?.play("spotify:track:08mG3Y1vljYA6bvDt4Wqkj")
            }
            view.get()!!.disappearText()
        }
    }

    fun workWithLoginResponse(response: AuthorizationResponse?) {
        if (view.get() == null) {
            return
        }
        if (response?.type != AuthorizationResponse.Type.TOKEN) {
            Timber.d("Download Spotify to use the feature!")
            view.get()!!.lockPlayButton(true)
        }
        else {
            view.get()!!.lockPlayButton(false)
        }
    }
}
