
package com.datalink.app.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit
) {
    var points by remember { mutableIntStateOf(120) }
    var rewardClaimed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "DataLink Uganda",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF101B35)
        )

        Text(
            text = "Your connection. Your rewards.",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF101B35)
            )
        ) {
            Column(
                modifier = Modifier.padding(22.dp)
            ) {
                Text(
                    text = "YOUR POINTS",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$points points",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Earn points and explore available rewards.",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        onNavigate("rewards")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF16C784)
                    )
                ) {
                    Text("View Rewards")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Explore DataLink",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF101B35)
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeatureCard(
            title = "Earn Free Data",
            description = "Complete activities and earn points.",
            buttonText = "Start Earning",
            onClick = { onNavigate("earn") }
        )

        FeatureCard(
            title = "Free Wi-Fi Finder",
            description = "Discover available public Wi-Fi locations.",
            buttonText = "Find Wi-Fi",
            onClick = { onNavigate("wifi") }
        )

        FeatureCard(
            title = "Daily Rewards",
            description = "Check in and collect your daily points.",
            buttonText = if (rewardClaimed) "Claimed Today" else "Claim Daily Points",
            onClick = {
                if (!rewardClaimed) {
                    points += 10
                    rewardClaimed = true
                }
            }
        )

        FeatureCard(
            title = "Invite Friends",
            description = "Invite people to join DataLink Uganda.",
            buttonText = "Invite Friends",
            onClick = { onNavigate("referrals") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "More",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF101B35)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { onNavigate("profile") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("My Profile")
        }

        OutlinedButton(
            onClick = { onNavigate("settings") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Settings")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "DataLink Uganda • Connect and earn",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101B35)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF16C784)
                )
            ) {
                Text(buttonText)
            }
        }
    }
}
