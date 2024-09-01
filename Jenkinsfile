pipeline {
    agent any

    tools {
        maven 'maven_3.8.7'
        jdk 'JDK21'
    }

    stages {
        stage("Clean") {
            steps {
                echo "Start Clean"
                bat "mvn clean"
                echo "Clean successful"
            }
        }
        stage("Test") {
            steps {
                echo "Start Test"
                catchError(buildResult: 'UNSTABLE', message: 'Tests failed') {
                    bat "mvn test"
                }
                echo "Test completed"
                junit '**/surefire-reports/**/*.xml'

            }
            // steps {
            //     echo "Start Test"
            //     bat "mvn test"
            //     echo "Test completed"
            // }
        }
        // stage("Sonar") {
        //     steps {
        //         echo "Start Sonar"
        //         bat "mvn sonar:sonar"
        //         echo "Sonar completed"
        //     }
        // }
        stage("Build") {
            steps {
                echo "Start Build"
                bat "mvn install -DskipTests"
                echo "Build completed"
            }
        }
        stage('Confirm Deploy to Staging') {
            steps {
            input(message: 'Deploy to Docker', ok: 'Yes')
            }
        }
        stage("Containerise and Send Email") {
            parallel {
                stage('Send Email on Build Success') {
                    steps {
                        script {
                            mail bcc: '', body: 'Hello, This is an email from Jenkins pipeline. Build is successful.', cc: '', from: '', replyTo: '', subject: 'Build Successful', to: 'nchinling@gmail.com'
                        }
                    }
                }
                stage('Docker Operations') {
                    stages {
                        stage('Build Docker Image') {
                            steps {
                                script {
                                    echo "Building Docker image"
                                    bat 'docker build -t nchinling/jenkins_lovecalc_repo:latest .'
                                }
                            }
                        }
                        stage('Push Docker Image') {
                            steps {
                                script {
                                    echo "Pushing Docker image"
                                    withCredentials([usernamePassword(credentialsId: 'nchinling-dockerhub', passwordVariable: 'dockerHubPassword', usernameVariable: 'dockerHubUser')]) {
                                        bat "docker login -u ${env.dockerHubUser} -p ${env.dockerHubPassword}"
										echo "Login to Docker Hub succeeded"
                                        bat 'docker push nchinling/jenkins_lovecalc_repo:latest '
										echo "Image pushed to Docker Hub successfully"
                                    }
                                }
                            }
                        }
						stage('Send Email on successful push to Docker Hub') {
							steps {
								script {
									mail bcc: '', body: 'Hello, This is an email from Jenkins pipeline. Push to Docker Hub is successful.', cc: '', from: '', replyTo: '', subject: 'Docker Build and Push Successful', to: 'nchinling@gmail.com'
								}
							}
						}
                    }
                }
            }
        }
    }

    post {
        success {
            // Actions to perform if the pipeline succeeds
            echo 'All stages passed successfully.'
            
        }
        failure {
            script {
                mail bcc: '', body: 'Hello, This is an email from Jenkins pipeline. Build or containerisation has failed. Please check the logs for details.', cc: '', from: '', replyTo: '', subject: 'Build / Containerisation Failed', to: 'nchinling@gmail.com'
            }
        }
    }
}
