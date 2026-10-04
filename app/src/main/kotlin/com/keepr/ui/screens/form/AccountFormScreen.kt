package com.keepr.ui.screens.form

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.keepr.data.model.VaultCategory
import com.keepr.ui.components.KeeprPrimaryButton
import com.keepr.ui.components.KeeprTextField
import com.keepr.ui.theme.AccentPurple
import com.keepr.ui.theme.AccentPurpleContainer
import com.keepr.ui.theme.AccentPurpleLight
import com.keepr.ui.theme.Background
import com.keepr.ui.theme.BorderDefault
import com.keepr.ui.theme.FavoriteActive
import com.keepr.ui.theme.KeeprTypography
import com.keepr.ui.theme.SurfaceMid
import com.keepr.ui.theme.TextSecondary
import com.keepr.ui.theme.TextTertiary

@Composable
fun AccountFormScreen(
    viewModel: AccountFormViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onOpenGenerator: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    var passwordVisible by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = TextSecondary)
            }
            Text(
                text = if (state.isEditing) "Edit account" else "Add account",
                style = KeeprTypography.titleLarge,
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
            IconButton(
                onClick = { viewModel.save() },
                enabled = state.serviceName.isNotBlank()
            ) {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = "Save",
                    tint = if (state.serviceName.isNotBlank()) AccentPurple else TextTertiary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            KeeprTextField(
                value = state.serviceName,
                onValueChange = { viewModel.onServiceName(it) },
                placeholder = "e.g. GitHub",
                label = "Service name",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            KeeprTextField(
                value = state.accountLabel,
                onValueChange = { viewModel.onAccountLabel(it) },
                placeholder = "e.g. Personal, Work",
                label = "Account label (optional)"
            )

            KeeprTextField(
                value = state.username,
                onValueChange = { viewModel.onUsername(it) },
                placeholder = "Username",
                label = "Username",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            KeeprTextField(
                value = state.email,
                onValueChange = { viewModel.onEmail(it) },
                placeholder = "email@example.com",
                label = "Email",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            KeeprTextField(
                value = state.password,
                onValueChange = { viewModel.onPassword(it) },
                placeholder = "Password",
                label = "Password",
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                trailingContent = {
                    Row {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onOpenGenerator,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Casino,
                                contentDescription = "Generate password",
                                tint = AccentPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            )

            KeeprTextField(
                value = state.website,
                onValueChange = { viewModel.onWebsite(it) },
                placeholder = "https://",
                label = "Website",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                )
            )

            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceMid)
                        .border(1.dp, BorderDefault, RoundedCornerShape(12.dp))
                        .clickable { categoryExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text("Category", style = KeeprTypography.labelMedium.copy(color = TextTertiary))
                    Spacer(Modifier.height(2.dp))
                    Text(state.category, style = KeeprTypography.bodyLarge)
                }
                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    VaultCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.displayName) },
                            onClick = {
                                viewModel.onCategory(cat.displayName)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            KeeprTextField(
                value = state.notes,
                onValueChange = { viewModel.onNotes(it) },
                placeholder = "Add notes...",
                label = "Notes",
                singleLine = false,
                minLines = 3,
                maxLines = 6
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceMid)
                    .border(1.dp, BorderDefault, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (state.isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = if (state.isFavorite) FavoriteActive else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Favorite",
                    style = KeeprTypography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.isFavorite,
                    onCheckedChange = { viewModel.onFavorite(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentPurple,
                        checkedTrackColor = AccentPurpleContainer
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            KeeprPrimaryButton(
                text = if (state.isEditing) "Save changes" else "Add account",
                onClick = { viewModel.save() },
                enabled = state.serviceName.isNotBlank()
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
