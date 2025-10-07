package com.dev.thecodecup.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import com.dev.thecodecup.ui.components.Header
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp
import com.dev.thecodecup.model.db.order.OrderViewModel
import com.dev.thecodecup.ui.components.BottomNavBar
import com.dev.thecodecup.ui.components.HistoryList
import com.dev.thecodecup.ui.components.OnGoingList
import com.dev.thecodecup.ui.theme.poppinsFontFamily

@Composable
fun OrderScreen(
    orderViewModel: OrderViewModel,
    onNavClick: (String) -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(38.dp))
                Header(
                    title = "My Order"
                )

                val tabTitles = listOf("On Going", "History")
                val pagerState = rememberPagerState(pageCount = { tabTitles.size })
                val coroutineScope = rememberCoroutineScope()

                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.White,
                    contentColor = Color(0xFF001833),
                    divider = {},
                    indicator = {}
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            },
                            text = { Text(
                                text = title,
                                color = if (pagerState.currentPage == index) Color(0xFF001833)
                                else Color(0xFFD8D8D8),
                                modifier = Modifier.padding(6.dp),
                                fontFamily = poppinsFontFamily,
                                fontSize = 16.sp
                            ) }
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    val indicatorWidthCollapsed = 20.dp
                    val indicatorWidthExpanded = 100.dp
                    val indicatorHeight = 3.dp
                    val density = LocalDensity.current
                    val configuration = LocalConfiguration.current
                    val tabWidth = with(density) { (configuration.screenWidthDp.dp / tabTitles.size).toPx() }
                    val targetOffset = tabWidth * pagerState.currentPage + (tabWidth - with(density) { indicatorWidthExpanded.toPx() }) / 2
                    val animatedOffset by animateFloatAsState(
                        targetValue = targetOffset,
                        animationSpec = tween(durationMillis = 350),
                        label = "TabIndicatorOffset"
                    )
                    val animatedWidth by animateDpAsState(
                        targetValue = if (pagerState.isScrollInProgress) indicatorWidthCollapsed else indicatorWidthExpanded,
                        animationSpec = tween(durationMillis = 350),
                        label = "TabIndicatorWidth"
                    )
                    val animatedWidthPx = with(density) { animatedWidth.toPx() }
                    val indicatorStart = animatedOffset + (with(density) { indicatorWidthExpanded.toPx() } - animatedWidthPx) / 2
                    Canvas(modifier = Modifier
                        .fillMaxWidth()
                        .height(indicatorHeight)) {
                        drawLine(
                            color = Color(0xFF324A59),
                            start = Offset(indicatorStart, size.height / 2),
                            end = Offset(indicatorStart + animatedWidthPx, size.height / 2),
                            strokeWidth = with(density) { indicatorHeight.toPx() }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f),
                    verticalAlignment = Alignment.Top
                ) { page ->
                    when (page) {
                        0 -> {
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center) {
                                OnGoingList(
                                    orderViewModel = orderViewModel
                                )
                            }
                        }
                        1 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                HistoryList(
                                    orderViewModel = orderViewModel
                                )
                            }
                        }
                    }
                }

            }

            Box(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .width(350.dp)
                    .align(Alignment.BottomCenter)
                    .border(
                        width = 0.6.dp,
                        color = Color(0xFFD8D8D8),
                        shape = RoundedCornerShape(32.dp)
                    )
            ) {
                BottomNavBar(
                    currentRoute = "orders",
                    onItemSelected = onNavClick
                )
            }
        }
    }
}
