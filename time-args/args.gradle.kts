plugins{
    id("com.time.tdd.java-conventions")
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
}

dependencies{
    implementation(libs.lombok)
}