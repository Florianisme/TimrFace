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
		    steps {
			    script {
                    if (env.BRANCH_NAME == 'origin/master') {
                        androidApkUpload deobfuscationFilesPattern: 'mobile/build/outputs/mapping/release/mapping.txt', filesPattern: 'mobile/build/outputs/apk/release/mobile-release.apk', googleCredentialsId: 'Florianisme', rolloutPercentage: '100', trackName: 'beta'
                    } else if (env.BRANCH_NAME == 'origin/release'){
                        androidApkUpload deobfuscationFilesPattern: 'mobile/build/outputs/mapping/release/mapping.txt', filesPattern: 'mobile/build/outputs/apk/release/mobile-release.apk', googleCredentialsId: 'Florianisme', rolloutPercentage: '100', trackName: 'internal'
                    } else if (env.BRANCH_NAME == 'origin/develop'){
                        androidApkUpload deobfuscationFilesPattern: 'mobile/build/outputs/mapping/release/mapping.txt', filesPattern: 'mobile/build/outputs/apk/release/mobile-release.apk', googleCredentialsId: 'Florianisme', rolloutPercentage: '100', trackName: 'internal'
                    }
                }
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
