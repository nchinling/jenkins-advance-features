pipeline {
    agent any

    parameters {
        booleanParam(name: 'DEPLOY_TO_AWS', defaultValue: false, description: 'Deploy to AWS?')
    }

    tools {
        maven 'maven_3.8.7'
        jdk 'JDK21'
    }

    environment {
        AWS_DEFAULT_REGION = 'us-east-1'
        AWS_ACCOUNT_ID = '851725323495'
        ECR_REPOSITORY = 'love-calc'
        IMAGE_TAG = 'latest'
        EB_APPLICATION_NAME = 'love-calculator'
        EB_ENVIRONMENT_NAME = 'e-pwsnmbnqsj'
        S3_BUCKET = 'elasticbeanstalk-us-east-1-851725323495'
    }

    stages {
        stage('Clean') {
            steps {
                echo 'Start Clean'
                bat 'mvn clean'
                echo 'Clean successful'
            }
        }
        stage('Test') {
            steps {
                echo 'Start Test'
                catchError(buildResult: 'UNSTABLE', message: 'Tests failed') {
                    bat 'mvn test'
                }
                echo 'Test completed'
                junit '**/surefire-reports/**/*.xml'
            }
        }
        // stage("Sonar") {
        //     steps {
        //         echo "Start Sonar"
        //         //static code analysis and skipping tests which was previously run
        //         bat "mvn sonar:sonar -DskipTests"
        //         echo "Sonar completed"
        //     }
        // }
        stage('Build') {
            steps {
                echo 'Start Build'
                bat 'mvn install -DskipTests'
                echo 'Build completed'
            }
        }
        stage('Archive') {
            steps {
                echo 'Archiving artifacts...'
                // Archive the specified files or directories
                archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true, fingerprint: true
            }
        }
        stage('Confirm Deploy to Docker') {
            steps {
                input(message: 'Deploy to Docker', ok: 'Yes')
            }
        }
        stage('Containerise and Send Email') {
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
                                    echo 'Building Docker image'
                                    bat 'docker build -t nchinling/jenkins_lovecalc_repo:latest .'
                                }
                            }
                        }
                        stage('Push Docker Image') {
                            steps {
                                script {
                                    echo 'Pushing Docker image'
                                    withCredentials([usernamePassword(credentialsId: 'nchinling-dockerhub', passwordVariable: 'dockerHubPassword', usernameVariable: 'dockerHubUser')]) {
                                        bat "docker login -u ${env.dockerHubUser} -p ${env.dockerHubPassword}"
                                        echo 'Login to Docker Hub succeeded'
                                        bat 'docker push nchinling/jenkins_lovecalc_repo:latest '
                                        echo 'Image pushed to Docker Hub successfully'
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
                stage('Trigger to AWS') {
                    when {
                        expression { return params.DEPLOY_TO_AWS }
                    }
                    echo 'Deploying to AWS'

                    stage('Build ECR docker image') {
                        steps {
                            script {
                                docker.build("${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${ECR_REPOSITORY}:${IMAGE_TAG}")
                            }
                        }
                    }

                    stage('Push to ECR') {
                        steps {
                            script {
                                withAWS(credentials: 'aws-jenkins', region: "${AWS_DEFAULT_REGION}") {
                                    bat 'aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 851725323495.dkr.ecr.us-east-1.amazonaws.com'
                                    bat 'docker tag love-calc:latest 851725323495.dkr.ecr.us-east-1.amazonaws.com/love-calc:latest'
                                    bat 'docker push 851725323495.dkr.ecr.us-east-1.amazonaws.com/love-calc:latest'
                                }
                            }
                        }
                    }

                    stage('Deploy to Elastic Beanstalk') {
                        steps {
                            script {
                                // Zip the Dockerrun.aws.json for Elastic Beanstalk deployment
                                bat 'zip -r deployment-package.zip Dockerrun.aws.json'

                                // Create a new application version and update the environment
                                withAWS(credentials: 'aws-jenkins', region: "${AWS_DEFAULT_REGION}") {
                                    bat "aws s3 cp deployment-package.zip s3://${S3_BUCKET}/${EB_APPLICATION_NAME}-${IMAGE_TAG}.zip"
                                    bat "aws elasticbeanstalk create-application-version --application-name ${EB_APPLICATION_NAME} --version-label ${IMAGE_TAG} --source-bundle S3Bucket=${S3_BUCKET},S3Key=${EB_APPLICATION_NAME}-${IMAGE_TAG}.zip"
                                    bat "aws elasticbeanstalk update-environment --application-name ${EB_APPLICATION_NAME} --environment-name ${EB_ENVIRONMENT_NAME} --version-label ${IMAGE_TAG}"
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
