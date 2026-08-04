package com.red.sovereign.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.red.sovereign.auth.AuthState
import com.red.sovereign.auth.AuthViewModel
import com.red.sovereign.ui.theme.AqyalCyanGlow
import com.red.sovereign.ui.theme.AqyalGold

@Composable
fun AuthFlow(viewModel: AuthViewModel) {
    when (val state = viewModel.state) {
        AuthState.Loading, AuthState.Submitting -> LoadingScreen()
        AuthState.Welcome -> WelcomeScreen(viewModel::showRegister, viewModel::showLogin)
        AuthState.Register -> RegisterScreen(viewModel::register, viewModel::showWelcome)
        AuthState.Login -> LoginScreen(viewModel::login, viewModel::showRecovery, viewModel::showWelcome)
        AuthState.Recovery -> RecoveryScreen(viewModel::recover, viewModel::showLogin)
        AuthState.RecoveryComplete -> StatusScreen("تم تغيير كلمة المرور", "أُلغيت كل الجلسات القديمة. يمكنك تسجيل الدخول الآن.", viewModel::showLogin)
        is AuthState.Pending -> PendingScreen(state, viewModel::checkApproval, viewModel::showLogin)
        is AuthState.Rejected -> StatusScreen("تم رفض الطلب", state.reason ?: "راجع مسؤول منظومة RED المحلية", viewModel::showLogin)
        AuthState.Suspended -> StatusScreen("الحساب موقوف", "تواصل مع المسؤول المحلي", viewModel::showLogin)
        AuthState.Banned -> StatusScreen("الحساب محظور", "تم إلغاء صلاحية الحساب والأجهزة", viewModel::showLogin)
        is AuthState.Error -> StatusScreen("تعذر إكمال العملية", state.message, viewModel::showWelcome)
        is AuthState.Authenticated -> Unit
    }
}

@Composable private fun LoadingScreen() = Centered { CircularProgressIndicator(color = AqyalGold); Spacer(Modifier.height(16.dp)); Text("جارٍ الاتصال بخادم RED المحلي…") }

@Composable
private fun WelcomeScreen(register: () -> Unit, login: () -> Unit) = Centered {
    Icon(Icons.Default.AdminPanelSettings, null, tint = AqyalGold, modifier = Modifier.height(90.dp))
    Text("RED", fontSize = 42.sp, fontWeight = FontWeight.Black, color = AqyalGold)
    Text("هوية محلية، محادثات سيادية، دون رقم هاتف", color = AqyalCyanGlow, textAlign = TextAlign.Center)
    Spacer(Modifier.height(40.dp))
    Button(register, Modifier.fillMaxWidth()) { Text("إنشاء حساب جديد") }
    Spacer(Modifier.height(12.dp))
    OutlinedButton(login, Modifier.fillMaxWidth()) { Text("لدي حساب") }
}

@Composable
private fun RegisterScreen(submit: (String, String, String) -> Unit, back: () -> Unit) {
    var name by remember { mutableStateOf("") }; var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }; var confirm by remember { mutableStateOf("") }
    val valid = name.trim().length >= 2 && username.matches(Regex("^[A-Za-z][A-Za-z0-9_.]{2,31}$")) && password.length >= 10 && password == confirm
    FormColumn("طلب حساب RED") {
        Text("سيبقى الحساب معلقاً حتى موافقة المسؤول. لا نطلب رقم هاتف أو شريحة.", textAlign = TextAlign.Center)
        Field(name, { name = it }, "الاسم الظاهر")
        Field(username, { username = it }, "اسم المستخدم", keyboard = KeyboardOptions(imeAction = ImeAction.Next))
        PasswordField(password, { password = it }, "كلمة المرور — 10 أحرف على الأقل")
        PasswordField(confirm, { confirm = it }, "تأكيد كلمة المرور")
        Button({ submit(name, username, password) }, Modifier.fillMaxWidth(), enabled = valid) { Text("إنشاء المفاتيح وإرسال الطلب") }
        OutlinedButton(back, Modifier.fillMaxWidth()) { Text("رجوع") }
    }
}

@Composable
private fun LoginScreen(submit: (String, String) -> Unit, recovery: () -> Unit, back: () -> Unit) {
    var username by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }
    FormColumn("تسجيل الدخول") {
        Field(username, { username = it }, "اسم المستخدم", leading = { Icon(Icons.Default.Person, null) })
        PasswordField(password, { password = it }, "كلمة المرور")
        Button({ submit(username, password) }, Modifier.fillMaxWidth(), enabled = username.isNotBlank() && password.isNotBlank()) { Text("دخول") }
        TextButton(recovery, Modifier.fillMaxWidth()) { Text("نسيت كلمة المرور؟ استخدم رمز الاستعادة") }
        OutlinedButton(back, Modifier.fillMaxWidth()) { Text("رجوع") }
    }
}

@Composable
private fun RecoveryScreen(submit: (String, String, String) -> Unit, back: () -> Unit) {
    var redId by remember { mutableStateOf("") }; var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }; var confirm by remember { mutableStateOf("") }
    FormColumn("استعادة محلية") {
        Text("لا نستخدم SMS أو بريداً. أدخل أحد الرموز التي حفظتها عند إنشاء الحساب.", textAlign = TextAlign.Center)
        Field(redId, { redId = it.uppercase() }, "RED ID")
        Field(code, { code = it.uppercase() }, "رمز الاستعادة")
        PasswordField(password, { password = it }, "كلمة المرور الجديدة")
        PasswordField(confirm, { confirm = it }, "تأكيد كلمة المرور")
        Button({ submit(redId, code, password) }, Modifier.fillMaxWidth(), enabled = redId.isNotBlank() && code.isNotBlank() && password.length >= 10 && password == confirm) { Text("تغيير وإلغاء الجلسات") }
        OutlinedButton(back, Modifier.fillMaxWidth()) { Text("رجوع") }
    }
}

@Composable
private fun PendingScreen(state: AuthState.Pending, check: () -> Unit, login: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    Centered {
        Icon(Icons.Default.AdminPanelSettings, null, tint = AqyalGold, modifier = Modifier.height(80.dp))
        Text("بانتظار موافقة المسؤول", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("تم إنشاء مفاتيح هويتك داخل الهاتف ولن يغادر المفتاح الخاص جهازك.", textAlign = TextAlign.Center)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(state.redId, color = AqyalGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton({ clipboard.setText(AnnotatedString(state.redId)) }) { Icon(Icons.Default.ContentCopy, "نسخ") }
        }
        if (state.recoveryCodes.isNotEmpty()) {
            Text("رموز الاستعادة — تظهر مرة واحدة", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            Text(state.recoveryCodes.joinToString("  "), fontSize = 11.sp, textAlign = TextAlign.Center)
            OutlinedButton({ clipboard.setText(AnnotatedString(state.recoveryCodes.joinToString("\n"))) }, Modifier.fillMaxWidth()) {
                Icon(Icons.Default.ContentCopy, null); Text(" نسخ الرموز وحفظها خارج الهاتف")
            }
        }
        Button(check, Modifier.fillMaxWidth()) { Icon(Icons.Default.CheckCircle, null); Text(" التحقق من الموافقة") }
        OutlinedButton(login, Modifier.fillMaxWidth()) { Text("الدخول لاحقاً") }
    }
}

@Composable private fun StatusScreen(title: String, description: String, action: () -> Unit) = Centered {
    Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.height(72.dp))
    Text(title, fontSize = 25.sp, fontWeight = FontWeight.Bold)
    Text(description, textAlign = TextAlign.Center)
    Spacer(Modifier.height(20.dp)); Button(action, Modifier.fillMaxWidth()) { Text("عودة") }
}

@Composable private fun Centered(content: @Composable ColumnScope.() -> Unit) = Column(Modifier.fillMaxSize().padding(28.dp), Arrangement.Center, Alignment.CenterHorizontally, content = content)
@Composable private fun FormColumn(title: String, content: @Composable ColumnScope.() -> Unit) = Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.spacedBy(14.dp), Alignment.CenterHorizontally) { Spacer(Modifier.height(35.dp)); Text(title, fontSize = 30.sp, fontWeight = FontWeight.Black, color = AqyalGold); content() }

@Composable private fun Field(value: String, change: (String) -> Unit, label: String, keyboard: KeyboardOptions = KeyboardOptions.Default, leading: (@Composable () -> Unit)? = null) = OutlinedTextField(value, change, Modifier.fillMaxWidth(), label = { Text(label) }, leadingIcon = leading, singleLine = true, keyboardOptions = keyboard)
@Composable private fun PasswordField(value: String, change: (String) -> Unit, label: String) = OutlinedTextField(value, change, Modifier.fillMaxWidth(), label = { Text(label) }, leadingIcon = { Icon(Icons.Default.Lock, null) }, singleLine = true, visualTransformation = PasswordVisualTransformation())
