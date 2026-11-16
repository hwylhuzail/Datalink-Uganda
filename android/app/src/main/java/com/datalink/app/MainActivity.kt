
package com.datalink.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.datalink.app.ui.theme.DataLinkTheme
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DataLinkTheme {
                DataLinkApp()
            }
        }
    }
}

@Composable
fun DataLinkApp() {
    var points by remember { mutableIntStateOf(120) }
    var claimed by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "DataLink",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                    Text(
                        "Your connection, your rewards.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text("🇺🇬", fontSize = 30.sp)
            }

            Spacer(Modifier.height(25.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Navy
                )
            ) {
                Column(Modifier.padding(22.dp)) {

                    Text(
                        "YOUR DATA BALANCE",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "0 MB",
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Redeem earned rewards for mobile data",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(18.dp))

                    Text(
                        "REWARD POINTS",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    Text(
                        "$points points",
                        color = Green,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Text(
                "Explore DataLink",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )

            Spacer(Modifier.height(14.dp))

            FeatureCard("🎁", "Earn Free Data",
                "Complete activities and earn rewards.")

            FeatureCard("📶", "Find Free Wi-Fi",
                "Discover public Wi-Fi opportunities.")

            FeatureCard("🔥", "Daily Rewards",
                "Come back every day to collect points.")

            FeatureCard("👥", "Invite Friends",
                "Share DataLink and earn referral rewards.")

            Spacer(Modifier.height(18.dp))

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
                    containerColor = Green
                )
            ) {
                Text(
                    if (claimed)
                        "Daily Reward Claimed ✓"
                    else
                        "Claim Daily Reward +20",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "DataLink Uganda • Version 1.0.0",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 11.sp,
                color = Color.Gray
            )
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
            Text(emoji, fontSize = 28.sp)

            Spacer(Modifier.width(15.dp))

            Column {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Navy
                )

                Text(
                    description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
