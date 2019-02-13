package io.euruapp.util

import io.euruapp.model.Job

interface JobClickListener {
    fun onClick(position: Int, data: Job)
}