package com.iptv.ccomate.ui.screens

import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun UserInfoScreen(
    onSubmit: (dni: String, name: String, phone: String) -> Unit
) {
    var dni by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var dniError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val dniFocus = remember { FocusRequester() }
    val nameFocus = remember { FocusRequester() }
    val phoneFocus = remember { FocusRequester() }
    val buttonFocus = remember { FocusRequester() }

    val isDniValid = dni.length == 8 && dni.all { it.isDigit() }
    val isPhoneValid = phone.length == 10 && phone.all { it.isDigit() }
    val isNameValid = name.isNotBlank()

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
                if (input.all { it.isDigit() } && input.length <= 8) {
                    dni = input
                }
            },
            label = { Text("DNI") },
            modifier = Modifier
                .widthIn(max = 400.dp)
                .focusRequester(dniFocus)
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown &&
                        event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                    ) {
                        nameFocus.requestFocus()
                        true
                    } else false
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            modifier = Modifier
                .widthIn(max = 400.dp)
                .focusRequester(nameFocus)
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when (event.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                phoneFocus.requestFocus()
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_UP -> {
                                dniFocus.requestFocus()
                                true
                            }
                            else -> false
                        }
                    } else false
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { input ->
                if (input.all { it.isDigit() } && input.length <= 10) {
                    phone = input
                }
            },
            label = { Text("Teléfono Celular") },
            modifier = Modifier
                .widthIn(max = 400.dp)
                .focusRequester(phoneFocus)
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when (event.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                buttonFocus.requestFocus()
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_UP -> {
                                nameFocus.requestFocus()
                                true
                            }
                            else -> false
                        }
                    } else false
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            onClick = { onSubmit(dni, name, phone) },
            enabled = isDniValid && isNameValid && isPhoneValid,
            modifier = Modifier
                .focusRequester(buttonFocus)
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown &&
                        event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_UP
                    ) {
                        phoneFocus.requestFocus()
                        true
                    } else false
                }
        ) {
            Text("Continuar")
        }
    }
}
