package com.weatheralert

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.weatheralert.ui.theme.Black
import com.weatheralert.ui.theme.GrayD
import com.weatheralert.ui.theme.GrayL
import com.weatheralert.ui.theme.GreenL
import com.weatheralert.ui.theme.WeatherAlertTheme
import com.weatheralert.ui.theme.White

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAlertTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginMenu(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun LoginMenu(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var rememberme by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val activity = LocalContext.current as? Activity

    Box(modifier = Modifier.fillMaxSize()){
        Image(
            painter = painterResource(id = R.drawable.imagem_1_login_menu),
            contentDescription = "Login background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
    }
    Column(
        modifier = modifier.padding(16.dp,135.dp).fillMaxSize().background(Color.Transparent,shape = RoundedCornerShape(25.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally,

        ) {
        Text(
            text = "WELCOME",
            fontSize = 24.sp,
            modifier = modifier.offset(0.dp,(-25).dp),
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = email,

            modifier = modifier.width(285.dp).height(50.dp).offset(0.dp, (-10).dp).border(
                3.dp,
                GrayL,
                RoundedCornerShape(25.dp)
            ),
            shape = RoundedCornerShape(25.dp),
            onValueChange = { email = it },
            placeholder = { Text("E-MAIL", color = GrayD, fontSize = 12.sp) }
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("PASSWORD", color = GrayD, fontSize = 12.sp) },
            shape = RoundedCornerShape(25.dp),
            modifier = modifier
                .width(285.dp)
                .height(50.dp)
                .border(3.dp, GrayL, RoundedCornerShape(25.dp)),

            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),


            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            }
        )
        Row{
            Checkbox(
                checked = rememberme, // Uma variável booleana que controla o estado do checkbox
                onCheckedChange = { newValue ->
                    rememberme = newValue // Atualize o estado conforme necessário
                },
                modifier = modifier.offset(0.dp,10.dp),
                enabled = true,
                colors = CheckboxColors(
                    checkedCheckmarkColor = White,
                    uncheckedCheckmarkColor = Color.Transparent, // Aqua80 (cor azul clara)
                    checkedBoxColor = GreenL, // Fundo verde quando marcado
                    uncheckedBoxColor = Color.Transparent, // Fundo branco quando desmarcado
                    disabledCheckedBoxColor =  GrayL ,
                    disabledUncheckedBoxColor = GrayL ,
                    disabledIndeterminateBoxColor = GrayL,
                    checkedBorderColor = GreenL,
                    uncheckedBorderColor = GrayD, // Borda cinza escura
                    disabledBorderColor = GrayL,
                    disabledUncheckedBorderColor = GrayL,
                    disabledIndeterminateBorderColor = GrayL
                )
            )
            Text(text = "Remember me?",
                fontSize = 12.sp,

                modifier = modifier.offset((-5).dp,28.dp)
            )

            Button(
                modifier = modifier.width(170.dp).offset(20.dp,10.dp),
                onClick = {
                    Toast.makeText(activity, "New Password!", Toast.LENGTH_LONG).show()
                },
                colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Black,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color.Transparent,
                )
            ) {
                Text("Forgot Passoword?",
                    fontSize = 12.sp)
            }
        }

        Button(
            modifier = modifier.width(285.dp).offset(0.dp,20.dp),

            onClick = {
                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(activity!!) { task ->
                        if (task.isSuccessful) {
                            if (rememberme) {
                                salvarCredenciais(context, email, password)
                            } else {
                                apagarCredenciais(context)
                            }
                            /*
                            activity.startActivity(
                                //Intent(activity, MainMenu::class.java).setFlags(FLAG_ACTIVITY_SINGLE_TOP)
                            )
                            */
                            Toast.makeText(activity, "Login OK!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(activity, "Login FAILED!", Toast.LENGTH_LONG).show()
                        }
                    }
            },


            colors = ButtonColors(
                containerColor = GreenL,
                contentColor = White,
                disabledContainerColor = GreenL,
                disabledContentColor = GreenL,
            )
        ) {
            Text("Login")
        }
        Button(
            modifier = modifier.width(285.dp).offset(0.dp,25.dp).background(Color.Transparent, shape = RoundedCornerShape(25.dp)),
            onClick = {
                Toast.makeText(activity, "Register!", Toast.LENGTH_LONG).show()
            },
            colors = ButtonColors(
                containerColor = GreenL,
                contentColor = White,
                disabledContainerColor = GreenL,
                disabledContentColor = GreenL,
            )
        ) {
            Text("Register")
        }
    }
}

// Função para salvar as credenciais
private fun salvarCredenciais(context: Context, email: String, senha: String) {
    val sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString("email", email)
        putString("senha", senha)
        putBoolean("lembrar", true)
        apply()
    }
}

// Função para apagar as credenciais ao fazer logout
private fun apagarCredenciais(context: Context) {
    val sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        clear()
        apply()
    }
}