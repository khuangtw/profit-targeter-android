// app/src/main/java/com/example/profittargeter/MainActivity.kt

package com.example.profittargeter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.profittargeter.model.StrategyRequest
import com.example.profittargeter.network.RetrofitClient
import com.example.profittargeter.ui.theme.ProfitTargeterTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfitTargeterTheme {
                Scaffold { innerPadding ->
                    StrategyScreen(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun StrategyScreen(modifier: Modifier = Modifier) {
    // 使用者輸入欄位
    var optionType by rememberSaveable { mutableStateOf("Call") }
    var ticker     by rememberSaveable { mutableStateOf("TSLA") }
    var strike     by rememberSaveable { mutableStateOf("300.0") }
    var entryPrice by rememberSaveable { mutableStateOf("2.5") }
    var dte        by rememberSaveable { mutableStateOf("10") }
    var delta      by rememberSaveable { mutableStateOf("0.52") }
    var theta      by rememberSaveable { mutableStateOf("-0.14") }
    var ivPercent  by rememberSaveable { mutableStateOf("45.0") }

    // 載入與錯誤狀態
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg  by remember { mutableStateOf<String?>(null) }
    var resultText by remember { mutableStateOf("結果會顯示在這裡") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Option Type
        OutlinedTextField(
            value = optionType,
            onValueChange = { optionType = it },
            label = { Text("Option Type (Call/Put)") },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )

        // Ticker
        OutlinedTextField(
            value = ticker,
            onValueChange = { ticker = it.uppercase() },
            label = { Text("Ticker") },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )

        // Strike Price
        OutlinedTextField(
            value = strike,
            onValueChange = { strike = it },
            label = { Text("Strike Price") },
            isError = strike.toDoubleOrNull() == null,
            singleLine = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        if (strike.toDoubleOrNull() == null) {
            Text(
                text = "請輸入有效的行使價",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Entry Price
        OutlinedTextField(
            value = entryPrice,
            onValueChange = { entryPrice = it },
            label = { Text("Entry Price") },
            isError = entryPrice.toDoubleOrNull() == null,
            singleLine = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        if (entryPrice.toDoubleOrNull() == null) {
            Text(
                text = "請輸入有效的買入價格",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Days to Expiration
        OutlinedTextField(
            value = dte,
            onValueChange = { dte = it },
            label = { Text("Days to Expiration") },
            isError = dte.toIntOrNull() == null,
            singleLine = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        if (dte.toIntOrNull() == null) {
            Text(
                text = "請輸入有效的到期天數",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Delta
        OutlinedTextField(
            value = delta,
            onValueChange = { delta = it },
            label = { Text("Delta (0~1)") },
            isError = delta.toDoubleOrNull() == null,
            singleLine = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        if (delta.toDoubleOrNull() == null) {
            Text(
                text = "請輸入有效的 Delta",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Theta
        OutlinedTextField(
            value = theta,
            onValueChange = { theta = it },
            label = { Text("Theta (負值)") },
            isError = theta.toDoubleOrNull() == null,
            singleLine = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        if (theta.toDoubleOrNull() == null) {
            Text(
                text = "請輸入有效的 Theta",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // IV Percent
        OutlinedTextField(
            value = ivPercent,
            onValueChange = { ivPercent = it },
            label = { Text("IV Percent (e.g. 45.0)") },
            isError = ivPercent.toDoubleOrNull() == null,
            singleLine = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        if (ivPercent.toDoubleOrNull() == null) {
            Text(
                text = "請輸入有效的 IV 百分比",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(16.dp))

        // 取得策略按鈕
        Button(
            onClick = {
                // 欄位驗證
                if (strike.toDoubleOrNull()==null
                    || entryPrice.toDoubleOrNull()==null
                    || dte.toIntOrNull()==null
                    || delta.toDoubleOrNull()==null
                    || theta.toDoubleOrNull()==null
                    || ivPercent.toDoubleOrNull()==null
                ) {
                    errorMsg = "請先修正上方紅字提示的欄位"
                    return@Button
                }
                errorMsg = null
                isLoading = true

                scope.launch {
                    try {
                        val req = StrategyRequest(
                            option_type        = optionType,
                            underlying_ticker  = ticker,
                            strike_price       = strike.toDouble(),
                            entry_price        = entryPrice.toDouble(),
                            days_to_expiration = dte.toInt(),
                            delta              = delta.toDouble(),
                            theta              = theta.toDouble(),
                            iv_percent         = ivPercent.toDouble()
                        )
                        val resp = RetrofitClient.service.getStrategy(req)

                        // 格式化四分位為百分比
                        val p25 = String.format("%.1f%%", resp.q25 * 100)
                        val p50 = String.format("%.1f%%", resp.q50 * 100)
                        val p75 = String.format("%.1f%%", resp.q75 * 100)

                        resultText = buildString {
                            append("止損價: ${resp.stop_loss_price} (${resp.stop_loss_pct})\n")
                            append("目標價: ${resp.target_profit_price} (${resp.target_profit_pct})\n")
                            append("IV 四分位: 25%=$p25, 50%=$p50, 75%=$p75\n")
                            append("Notes:\n")
                            resp.notes.forEach { append(" • $it\n") }
                            resp.exit_reminder?.takeIf { it.isNotBlank() }?.let {
                                append("提醒: $it")
                            }
                        }
                    } catch (e: Exception) {
                        errorMsg = "錯誤：${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("取得策略")
            }
        }

        // 全域錯誤提示
        errorMsg?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // 顯示結果
        Text(
            text = resultText,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}
