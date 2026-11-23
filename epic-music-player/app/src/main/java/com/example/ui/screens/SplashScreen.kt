package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    var scale by remember { mutableStateOf(0f) }
    
    // Smooth zoom in animation from 0% to 100% scale
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    LaunchedEffect(Unit) {
        scale = 1f
        // Stay on screen for 3 seconds, then trigger timeout to transition
        delay(3000)
        onTimeout()
    }

    // Gorgeous deep gradient background
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0C1B), // Dark space blue
            Color(0xFF1F1A3A), // Deep cosmic violet
            Color(0xFF0F0C1B)
        )
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        val logoWidth = maxWidth * 0.4f // Strictly 40% of screen width (Requirement 3)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Invisible placeholder to balance vertical arrangement
            Spacer(modifier = Modifier.height(30.dp))

            // Upper Middle Area: Animated App Logo & Player Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Application Logo with Circular frame & border, Zoom-In scale animation
                Box(
                    modifier = Modifier
                        .size(logoWidth)
                        .scale(animatedScale)
                        .clip(CircleShape)
                        .background(Color(0xFF2E2452).copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_app_logo_1781381624443),
                        contentDescription = "Epic Music Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Epic Music Player (Big uppercase text)
                Text(
                    text = "EPIC MUSIC PLAYER",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // ~Enjoy the Ride~ (Medium text)
                Text(
                    text = "~Enjoy the Ride~",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFFE91E63), // Stylish golden-pink brand color
                    textAlign = TextAlign.Center
                )
            }

            // Bottom Area: Branding, Credits & Telegram details
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "-----------------",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.3f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(6.dp))

                // By: aydin mirzaie (Lowercase, small font)
                Text(
                    text = "by: aydin mirzaie",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Telegram: @ideen (Lowercase, even smaller font)
                Text(
                    text = "telegram: @ideen",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
