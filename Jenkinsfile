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
	    stage('Publish to Play Store') {
			when {
				anyOf {
					branch 'master'
				}
			}
		    steps {
			    androidApkUpload deobfuscationFilesPattern: 'mobile/build/outputs/mapping/release/mapping.txt', filesPattern: 'mobile/build/outputs/apk/release/mobile-release.apk', googleCredentialsId: 'Florianisme', rolloutPercentage: '100', trackName: 'beta'
	    }
	    }
    }
	post {
		failure {
			script {
				currentBuild.result = 'FAILURE'
			}
		}
		unstable {
			script {
				currentBuild.result = 'UNSTABLE'
			}
		}
		always {
			step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: emailextrecipients([[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']])])
		}
	}
}
