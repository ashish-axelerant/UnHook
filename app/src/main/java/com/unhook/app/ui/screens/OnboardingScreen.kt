// UnHook — 4-step onboarding flow (welcome, profile, pairing, confirmation)
package com.unhook.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unhook.app.R

private val avatarOptions = listOf("😊", "🦁", "🐯", "🦊", "🐺", "🦅", "🏄", "🧗")

@Composable
fun OnboardingScreen(
    onComplete: (userName: String, userAvatar: String, partnerName: String, partnerAvatar: String, pairingCode: String) -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    var userName by remember { mutableStateOf("") }
    var userAvatar by remember { mutableStateOf(avatarOptions[0]) }
    var partnerName by remember { mutableStateOf("") }
    var partnerAvatar by remember { mutableStateOf(avatarOptions[1]) }
    var pairingCode by remember { mutableStateOf("") }
    var isCreatingRoom by remember { mutableStateOf<Boolean?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        AnimatedContent(targetState = step, label = "onboarding") { currentStep ->
            when (currentStep) {
                0 -> WelcomeStep(onNext = { step = 1 })
                1 -> ProfileStep(
                    name = userName,
                    onNameChange = { userName = it },
                    selectedAvatar = userAvatar,
                    onAvatarChange = { userAvatar = it },
                    onNext = { step = 2 },
                )
                2 -> PairingStep(
                    isCreatingRoom = isCreatingRoom,
                    onCreateRoom = {
                        isCreatingRoom = true
                        pairingCode = (100000..999999).random().toString()
                    },
                    onHaveCode = { isCreatingRoom = false },
                    pairingCode = pairingCode,
                    onCodeChange = { pairingCode = it },
                    partnerName = partnerName,
                    onPartnerNameChange = { partnerName = it },
                    partnerAvatar = partnerAvatar,
                    onPartnerAvatarChange = { partnerAvatar = it },
                    onNext = { step = 3 },
                )
                3 -> ConfirmationStep(
                    partnerName = partnerName,
                    onStart = { onComplete(userName, userAvatar, partnerName, partnerAvatar, pairingCode) },
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "🎣", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text = stringResource(R.string.onboarding_get_started))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileStep(
    name: String,
    onNameChange: (String) -> Unit,
    selectedAvatar: String,
    onAvatarChange: (String) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.onboarding_profile_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.onboarding_name_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_pick_avatar),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            avatarOptions.forEach { emoji ->
                AvatarChip(
                    emoji = emoji,
                    isSelected = emoji == selectedAvatar,
                    onClick = { onAvatarChange(emoji) },
                )
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text = stringResource(R.string.onboarding_continue))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PairingStep(
    isCreatingRoom: Boolean?,
    onCreateRoom: () -> Unit,
    onHaveCode: () -> Unit,
    pairingCode: String,
    onCodeChange: (String) -> Unit,
    partnerName: String,
    onPartnerNameChange: (String) -> Unit,
    partnerAvatar: String,
    onPartnerAvatarChange: (String) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.onboarding_pairing_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))

        when (isCreatingRoom) {
            null -> {
                Button(
                    onClick = onCreateRoom,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = stringResource(R.string.onboarding_create_room))
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onHaveCode,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = stringResource(R.string.onboarding_have_code))
                }
            }
            true -> {
                Text(
                    text = stringResource(R.string.onboarding_your_code),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = pairingCode,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 8.sp,
                )
                Spacer(modifier = Modifier.height(24.dp))
                PartnerInfoFields(
                    partnerName = partnerName,
                    onPartnerNameChange = onPartnerNameChange,
                    partnerAvatar = partnerAvatar,
                    onPartnerAvatarChange = onPartnerAvatarChange,
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = partnerName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = stringResource(R.string.onboarding_continue))
                }
            }
            false -> {
                OutlinedTextField(
                    value = pairingCode,
                    onValueChange = { if (it.length <= 6) onCodeChange(it) },
                    label = { Text(stringResource(R.string.onboarding_enter_code_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(24.dp))
                PartnerInfoFields(
                    partnerName = partnerName,
                    onPartnerNameChange = onPartnerNameChange,
                    partnerAvatar = partnerAvatar,
                    onPartnerAvatarChange = onPartnerAvatarChange,
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = pairingCode.length == 6 && partnerName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = stringResource(R.string.onboarding_connect))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PartnerInfoFields(
    partnerName: String,
    onPartnerNameChange: (String) -> Unit,
    partnerAvatar: String,
    onPartnerAvatarChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = partnerName,
        onValueChange = onPartnerNameChange,
        label = { Text(stringResource(R.string.onboarding_partner_name_hint)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.onboarding_partner_avatar),
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        avatarOptions.forEach { emoji ->
            AvatarChip(
                emoji = emoji,
                isSelected = emoji == partnerAvatar,
                onClick = { onPartnerAvatarChange(emoji) },
            )
        }
    }
}

@Composable
private fun ConfirmationStep(
    partnerName: String,
    onStart: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "🎉", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_all_set),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_paired_with, partnerName),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.onboarding_lets_go),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text(text = stringResource(R.string.onboarding_start))
        }
    }
}

@Composable
private fun AvatarChip(
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
            )
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = 24.sp)
    }
}
