package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AgeGate

/** Neutral age screen: asks for the birth year without hinting at any "right" answer. */
@Composable
fun AgeGateScreen(onConfirm: (Int) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE0B2))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Bienvenido!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6D4C41)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Para ajustar la experiencia, indica tu año de nacimiento.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF6D4C41)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = text,
            onValueChange = {
                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                    text = it
                    error = false
                }
            },
            label = { Text("Año de nacimiento") },
            singleLine = true,
            isError = error,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.testTag("birth_year_field")
        )
        if (error) {
            Text(
                text = "Introduce un año válido.",
                fontSize = 12.sp,
                color = Color(0xFFB71C1C)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val year = text.toIntOrNull()
                if (year != null && AgeGate.isValidYear(year)) onConfirm(year) else error = true
            },
            modifier = Modifier.testTag("age_continue_button")
        ) {
            Text("Continuar")
        }
    }
}
