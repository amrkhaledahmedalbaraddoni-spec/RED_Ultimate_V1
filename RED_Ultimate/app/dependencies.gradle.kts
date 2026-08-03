dependencies {
  lintChecks(project(":lintchecks"))
  coreLibraryDesugaring(libs.android.tools.desugar)

  // RED Sovereign Core - Ultimate V2
  implementation(project(":lib:libsignal-service"))
  implementation(project(":core:util"))
  implementation(project(":core:ui"))
  implementation(libs.libsignal.android)
  implementation(libs.signal.android.database.sqlcipher)

  // System A: VoIP 4K AV1/VP9/H264
  implementation(libs.signal.ringrtc)
  implementation(libs.bundles.media3)
  implementation("org.webrtc:google-webrtc:1.0.32006")
  implementation("org.mediasoup.droid:mediasoup-client:3.4.0")

  // System B: PSTN DINSTAR UC2000
  implementation("org.asteriskjava:asterisk-java:3.40.0")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")

  // System C: Messaging - Guaranteed Delivery UUID v7
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  kapt(libs.androidx.room.compiler)
  implementation(libs.kotlinx.serialization.json)
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

  // Hilt - Dependency Injection Ultimate
  implementation(libs.hilt.android)
  kapt(libs.hilt.compiler)
  implementation(libs.androidx.hilt.navigation.compose)

  // Master UI - Sovereign Theme
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.activity.compose)
  implementation(libs.material.material)
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material:material-icons-extended")

  // Security - Quantum Guard
  implementation("androidx.security:security-crypto:1.1.0-alpha06")
  implementation("androidx.datastore:datastore-preferences:1.0.0")

  // Media Compression - Ultimate
  implementation("com.github.bumptech.glide:glide:4.16.0")
  implementation("id.zelory:compressor:3.0.1")
}
