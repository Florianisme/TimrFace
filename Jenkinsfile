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
                    if (env.BRANCH_NAME == 'master') {
                        echo 'Publishing APK to Beta channel'
                        androidApkUpload deobfuscationFilesPattern: 'mobile/build/outputs/mapping/release/mapping.txt,wear/build/outputs/mapping/release/mapping.txt', filesPattern: 'mobile/build/outputs/apk/release/mobile-release.apk,wear/build/outputs/apk/release/mobile-release.apk', googleCredentialsId: 'Florianisme', rolloutPercentage: '100', trackName: 'beta'
                    } else if (env.BRANCH_NAME == 'release'){
                        echo 'Publishing APK to Internal channel'
                        androidApkUpload deobfuscationFilesPattern: 'mobile/build/outputs/mapping/release/mapping.txt,wear/build/outputs/mapping/release/mapping.txt', filesPattern: 'mobile/build/outputs/apk/release/mobile-release.apk,wear/build/outputs/apk/release/mobile-release.apk', googleCredentialsId: 'Florianisme', rolloutPercentage: '100', trackName: 'internal'
                    } else if (env.BRANCH_NAME == 'develop'){
                        echo 'Publishing APK to Internal channel'
                        try {
                            androidApkUpload deobfuscationFilesPattern: 'mobile/build/outputs/mapping/release/mapping.txt,wear/build/outputs/mapping/release/mapping.txt', filesPattern: 'mobile/build/outputs/apk/release/mobile-release.apk,wear/build/outputs/apk/release/mobile-release.apk', googleCredentialsId: 'Florianisme', rolloutPercentage: '100', trackName: 'internal'
                        } catch(error) {
                            currentBuild.result = 'SUCCESS'
                        }
                    } else {
                        echo 'Publishing criteria not met'
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
