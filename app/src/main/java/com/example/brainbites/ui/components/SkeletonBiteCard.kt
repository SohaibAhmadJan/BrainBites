package com.example.brainbites.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A placeholder skeleton card for BiteItem.
 * Uses the shimmer effect to indicate loading.
 */
@Composable
fun SkeletonBiteCard(
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Shimmer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 100.dp, height = 24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmer()
                )
                
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title Shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(22.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Snippet Shimmers
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Button Shimmer
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 120.dp, height = 16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
        }
    }
}
