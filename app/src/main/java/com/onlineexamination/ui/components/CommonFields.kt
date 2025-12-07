package com.onlineexamination.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(label: String, value: String, onValueChange: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            trailingIcon = { Icon(Icons.Default.CalendarToday, "Select Date") },
            colors = TextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                disabledIndicatorColor = MaterialTheme.colorScheme.outline
            )
        )
        Box(modifier = Modifier
            .matchParentSize()
            .clickable { showDialog = true })
    }

    if (showDialog) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = parseDateTimeToMillis(value, ""))
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Date") },
            text = { DatePicker(state = datePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onValueChange(formatter.format(Date(it))) }
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(label: String, value: String, onValueChange: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            trailingIcon = { Icon(Icons.Default.Schedule, "Select Time") },
            colors = TextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                disabledIndicatorColor = MaterialTheme.colorScheme.outline
            )
        )
        Box(modifier = Modifier
            .matchParentSize()
            .clickable { showDialog = true })
    }

    if (showDialog) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    cal.set(Calendar.MINUTE, timePickerState.minute)
                    onValueChange(formatter.format(cal.time))
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun DropdownField(
    label: String,
    selectedKey: String,
    options: List<DropdownOption>,
    enabled: Boolean = true,
    onOptionSelected: (DropdownOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.key == selectedKey }?.label ?: ""
    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp)) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { textFieldSize = it.size },
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    "Dropdown",
                    Modifier.clickable(enabled = enabled) { if (options.isNotEmpty()) expanded = true }
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(density) { textFieldSize.width.toDp() })
        ) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option.label) }, onClick = {
                    onOptionSelected(option)
                    expanded = false
                })
            }
        }
    }
}

fun parseDateTimeToMillis(dateString: String, timeString: String): Long {
    return try {
        val dateTimeString = "$dateString $timeString"
        val format = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault())
        format.parse(dateTimeString)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}
