package com.weatheralert

import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.weatheralert.ui.theme.GrayD
import com.weatheralert.ui.theme.GrayL
import com.weatheralert.ui.theme.GreenL
import com.weatheralert.ui.theme.Red
import com.weatheralert.ui.theme.White


@Preview(showBackground = true)
@Composable
fun RegisterMenu(modifier: Modifier = Modifier){
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var cpassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var cpasswordVisible by rememberSaveable { mutableStateOf(false) }
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
        //modifier = modifier.padding(19.dp,145.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally,

        modifier = modifier
            .padding(19.dp, 85.dp)
            .fillMaxSize()
            .border(2.dp, White, shape = RoundedCornerShape(25.dp)).background(White,shape = RoundedCornerShape(25.dp)),



        ) {
        OutlinedTextField(
            value = name,
            onValueChange = {name = it},
            shape = RoundedCornerShape(25.dp),
            placeholder = { Text("NAME", color = GrayD, fontSize = 12.sp) },
            modifier = modifier.width(285.dp).height(50.dp).offset(0.dp,(-20).dp).border(3.dp, GrayL, shape = RoundedCornerShape(25.dp))
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
            value = address,
            onValueChange = {address = it},
            shape = RoundedCornerShape(25.dp),
            modifier = modifier.width(285.dp).height(50.dp).offset(0.dp,0.dp).border(
                3.dp,
                GrayL,
                shape = RoundedCornerShape(25.dp)
            ),
            placeholder = { Text("ADDRESS (STREET & NUMBER)", color = GrayD, fontSize = 12.sp) }
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("PASSWORD", color = GrayD, fontSize = 12.sp) },
            shape = RoundedCornerShape(25.dp),
            modifier = modifier
                .width(285.dp)
                .height(50.dp).offset(0.dp,10.dp)
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


        OutlinedTextField(
            value = cpassword,
            onValueChange = { cpassword = it },
            placeholder = { Text("CONFIRM PASSWORD", color = GrayD, fontSize = 12.sp) },
            shape = RoundedCornerShape(25.dp),
            modifier = modifier
                .width(285.dp)
                .height(50.dp).offset(0.dp,20.dp)
                .border(3.dp, GrayL, RoundedCornerShape(25.dp)),

            visualTransformation = if (cpasswordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),


            trailingIcon = {
                val image = if (cpasswordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = { cpasswordVisible = !cpasswordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            }
        )
        Button(
            modifier = modifier.width(285.dp).offset(0.dp,25.dp).background(Color.Transparent, shape = RoundedCornerShape(25.dp)),
            onClick = {
                Firebase.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(activity!!) { task ->




                        if (task.isSuccessful) {
                            Toast.makeText(activity,"Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show()
                            activity.startActivity(
                                Intent(activity, MainActivity::class.java).setFlags(
                                    FLAG_ACTIVITY_SINGLE_TOP )
                            )
                            //FBDatabase().register(User(name, email,date,altura.toFloat(),sexoesco))

                        } else {
                            Toast.makeText(activity,
                                "Registro FALHOU!", Toast.LENGTH_LONG).show()
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
            Text("Confirm")
        }

        Button(
            modifier = modifier.width(285.dp).offset(0.dp,30.dp).background(Color.Transparent, shape = RoundedCornerShape(25.dp)),
            onClick = {
                activity?.startActivity(
                    Intent(activity, MainActivity::class.java).setFlags(
                        FLAG_ACTIVITY_SINGLE_TOP
                    )
                )
            },
            colors = ButtonColors(
                containerColor = Red,
                contentColor = White,
                disabledContainerColor = Red,
                disabledContentColor = Red,
            )
        ) {
            Text("Cancel")
        }
    }

}