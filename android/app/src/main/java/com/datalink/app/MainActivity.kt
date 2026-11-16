
package com.datalink.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF101B35)
private val Green = Color(0xFF16C784)
private val LightBackground = Color(0xFFF4F7FB)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DataLinkApp()
        }
    }
}

@Composable
fun DataLinkApp() {
    var points by remember { mutableIntStateOf(120) }
    var claimed by remember { mutableStateOf(false) }

    MaterialTheme {
        Scaffold(
            containerColor = LightBackground
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
            ) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DataLink",
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                        Text(
                            text = "Your connection, your rewards.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Text(
                        text = "🇺🇬",
                        fontSize = 30.sp
                    )
                }

                Spacer(modifier = Modifier.height(25.dp))

                // Data balance card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Navy
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp)
                    ) {
                        Text(
                            text = "YOUR DATA BALANCE",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "0 MB",
                            color = Color.White,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Redeem earned rewards for mobile data",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "REWARD POINTS",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )

                        Text(
                            text = "$points points",
                            color = Green,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Explore DataLink",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Feature cards
                FeatureCard(
                    emoji = "🎁",
                    title = "Earn Free Data",
                    description = "Complete activities and earn rewards."
                )

                FeatureCard(
                    emoji = "📶",
                    title = "Find Free Wi-Fi",
                    description = "Discover public Wi-Fi opportunities."
                )

                FeatureCard(
                    emoji = "🔥",
                    title = "Daily Rewards",
                    description = "Come back every day to collect points."
                )

                FeatureCard(
                    emoji = "👥",
                    title = "Invite Friends",
                    description = "Share DataLink and earn referral rewards."
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Daily reward button
                Button(
                    onClick = {
                        if (!claimed) {
                            points += 20
                            claimed = true
                        }
                    },
                    enabled = !claimed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    Text(
                        text = if (claimed)
                            "Daily Reward Claimed ✓"
                        else
                            "Claim Daily Reward +20",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "DataLink Uganda • Version 1.0.0",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun FeatureCard(
    emoji: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.width(15.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Navy
                )

                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
