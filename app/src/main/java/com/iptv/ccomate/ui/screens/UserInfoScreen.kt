package com.iptv.ccomate.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun UserInfoScreen(
    onSubmit: (dni: String, name: String, phone: String) -> Unit
) {
    var dni by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Estados para mensajes de error
    var dniError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    // Validar DNI (8 dígitos)
    val isDniValid = dni.length == 8 && dni.all { it.isDigit() }
    // Validar Teléfono (10 dígitos)
    val isPhoneValid = phone.length == 10 && phone.all { it.isDigit() }
    // Validar Nombre (no vacío)
    val isNameValid = name.isNotBlank()

    // Actualizar mensajes de error dinámicamente
    LaunchedEffect(dni) {
        dniError = when {
            dni.isEmpty() -> "Ingresa 8 dígitos"
            dni.length != 8 -> "El DNI debe tener 8 dígitos"
            !dni.all { it.isDigit() } -> "Solo se permiten números"
            else -> null
        }
    }

    LaunchedEffect(phone) {
        phoneError = when {
            phone.isEmpty() -> "Ingresa 10 dígitos"
            phone.length != 10 -> "El teléfono debe tener 10 dígitos"
            !phone.all { it.isDigit() } -> "Solo se permiten números"
            else -> null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Por favor, ingresa tus datos",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = dni,
            onValueChange = { input ->
                // Permitir solo números y máximo 8 dígitos
                if (input.all { it.isDigit() } && input.length <= 8) {
                    dni = input
                }
            },
            label = { Text("DNI") },
            modifier = Modifier.fillMaxWidth(0.8f),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            isError = dniError != null
        )
        dniError?.let {
            Text(
                text = it,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(0.8f),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { input ->
                // Permitir solo números y máximo 10 dígitos
                if (input.all { it.isDigit() } && input.length <= 10) {
                    phone = input
                }
            },
            label = { Text("Teléfono Celular") },
            modifier = Modifier.fillMaxWidth(0.8f),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            isError = phoneError != null
        )
        phoneError?.let {
            Text(
                text = it,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onSubmit(dni, name, phone)
            },
            enabled = isDniValid && isNameValid && isPhoneValid
        ) {
            Text("Continuar")
        }
    }
}