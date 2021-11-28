package com.chunkymonkey.pgntogifconverter.util

import java.io.*


class FileUtils {
    companion object {
        fun copyFile(input: File, output: File): Boolean {
            createParentDirIfNotExists(input)
            createParentDirIfNotExists(output)
            var `is`: FileInputStream? = null
            var os: FileOutputStream? = null
            try {
                `is` = FileInputStream(input)
                os = FileOutputStream(output)
                copyFile(`is`, os)
                return true
            } catch (e: IOException) {
                e.printStackTrace()
            }
            close(`is`)
            close(os)
            return false
        }

        fun copyFile(input: File, os: OutputStream): Boolean {
            createParentDirIfNotExists(input)
            var `is`: FileInputStream? = null
            try {
                `is` = FileInputStream(input)
                copyFile(`is`, os)
                return true
            } catch (e: IOException) {
                e.printStackTrace()
            }
            close(`is`)
            close(os)
            return true
        }

        @Throws(IOException::class)
        fun copyFile(`is`: InputStream, os: OutputStream) {
            val buffer = ByteArray(1024)
            var len: Int
            while (`is`.read(buffer).also { len = it } != -1) {
                os.write(buffer, 0, len)
            }
            os.flush()
        }

        private fun close(closeable: Closeable?) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        private fun createParentDirIfNotExists(file: File) {
            val parentFile = file.parentFile
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs()
            }
        }
    }
}