pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh script: 'chmod +x gradlew'
                sh label: 'Gradle Build', script: './gradlew --parallel --max-workers=8  clean assembleRelease'
            }
        }
        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'mobile/build/outputs/apk/release/mobile-release.apk', followSymlinks: false, onlyIfSuccessful: true
                archiveArtifacts artifacts: 'mobile/build/outputs/mapping/release/mapping.txt', followSymlinks: false, onlyIfSuccessful: true
            }
        }
    }

    post {
      success {
        slackSend channel: 'fme_jenkinsci', color: 'good', message: 'Job ${env.JOB_NAME} finished with status SUCCESSFUL \\nBuild Number: ${env.BUILD_NUMBER} \\nSee more: (<${env.BUILD_URL}|Build URL>)'
      }
    }
}