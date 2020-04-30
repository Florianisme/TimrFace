pipeline {
    agent any

    stages {
        stage('SCM Checkout') {
            steps {
                cleanWs()
                checkout([$class: 'GitSCM', branches: [[name: '*/master']] userRemoteConfigs: [[credentialsId: '8f84c8ac-0ef9-40c3-a45d-7cd9a5831cc4', url: 'https://github.com/Florianisme/TimrFace.git']]])
            }
        }
        stage('Build') {
            steps {
                sh label: 'Gradle Build', script: 'gradlew clean assembleRelease'
            }
        }
        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'mobile/build/outputs/apk/release/mobile-release.apk', followSymlinks: false, onlyIfSuccessful: true
                archiveArtifacts artifacts: '/mobile/build/outputs/mapping/release/mapping.txt', followSymlinks: false, onlyIfSuccessful: true
            }
        }
    }
}