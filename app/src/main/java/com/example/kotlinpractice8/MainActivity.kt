@file:Suppress("DEPRECATION")

package com.example.kotlinpractice8

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.kotlinpractice8.ui.theme.KotlinPractice8Theme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KotlinPractice8Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainActivityScreen(applicationContext)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityScreen(context: Context) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menu", modifier = Modifier.padding(16.dp))
                Divider()
                NavigationDrawerItem(
                    label = { Text(text = "MainActivity3") },
                    selected = false,
                    onClick = {
                        val intent = Intent(context, MainActivity3::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "MainActivity2") },
                    selected = false,
                    onClick = {
                        val intent = Intent(context, MainActivity2::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    }
                )
                // ...other drawer items
            }
        },
    ) {
        Scaffold(
            topBar = {
                AppBar(title = "MainActivity")
            },
            bottomBar = {
                BottomAppBar(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    IconButton(onClick = {
                        val intent = Intent(context, MainActivity2::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    }) {
                        Icon(
                            Icons.Filled.Favorite, contentDescription
                            = "Favorite"
                        )
                    }
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text("Show drawer") },
                    icon = { Icon(Icons.Filled.Add, contentDescription = "") },
                    onClick = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    },
                )
            }
        ) { innerPadding ->
            // Основной контент окна Scaffold
            CustomContent(innerPadding = innerPadding, context = context)
        }
    }
}

@Composable
fun CustomContent(innerPadding: PaddingValues, context: Context) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Body(context = context)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(title: String) {
    TopAppBar(
        title = { Text(text = title) },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun Body(context: Context) {

    var imageUrl by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Ссылка") },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (imageUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUrl),
                        contentDescription = "Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        Button(
            onClick = {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        downloadImage(context, imageUrl)
                    } catch (e: Exception) {
                        Log.e("ASD", "Ошибка")
                    }

                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = "Сохранить")
        }
    }
}

//https://aambassador.com/images/car/car-3z.jpg

private suspend fun downloadImage(context: Context, imageUrl: String): String {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder().url(imageUrl).build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val fileName = "image_${System.currentTimeMillis()}.jpg"
                saveImageToDevice(context, response.body?.byteStream(), fileName)
                fileName
            } else {
                ""
            }
        }
    }
}

private suspend fun saveImageToDevice(
    context: Context,
    inputStream: InputStream?,
    fileName: String
) {
    withContext(Dispatchers.IO) {
        val directory = File(context.externalMediaDirs.first(), "photo")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)

        outputStream.flush()
        outputStream.close()
    }
}

@Composable
fun Greeting(innerPadding: PaddingValues, name: String) {
    Text(
        text = "Hello $name!",
        modifier = Modifier
            .padding(innerPadding)
    )
}