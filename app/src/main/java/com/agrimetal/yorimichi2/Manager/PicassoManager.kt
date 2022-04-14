package com.agrimetal.yorimichi2.Manager

import com.squareup.picasso.Picasso


import android.app.Application

class PicassoManager :Application(){
    var picasso: Picasso = Picasso.get()

    companion object {
        private var instance : PicassoManager? = null

        fun  getInstance(): PicassoManager {
            if (instance == null)
                instance = PicassoManager()

            return instance!!
        }
    }
}