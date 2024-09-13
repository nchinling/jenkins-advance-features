pipeline {
    agent any

    parameters {
        booleanParam(name: 'DEPLOY_TO_DOCKER', defaultValue: true, description: 'Deploy to Dockerhub?')
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
                archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true, fingerprint: true
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
                    when {
                        expression { return params.DEPLOY_TO_DOCKER }
                    }
                    steps {
                        script {
                            echo 'Building Docker image'
                            bat 'docker build -t nchinling/jenkins_lovecalc_repo:latest .'

                            echo 'Pushing Docker image'
                            withCredentials([usernamePassword(credentialsId: 'nchinling-dockerhub', passwordVariable: 'dockerHubPassword', usernameVariable: 'dockerHubUser')]) {
                                bat "docker login -u ${env.dockerHubUser} -p ${env.dockerHubPassword}"
                                bat 'docker push nchinling/jenkins_lovecalc_repo:latest'
                            }
                        }
                    }
                }
            }
        }
        stage('Confirm Deploy to AWS') {
            steps {
                input(message: 'Deploy to AWS', ok: 'Yes')
            }
        }
        stage('Trigger to AWS') {
            steps {
                echo 'Deploying to AWS'
                script {
                    // docker.build("${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${ECR_REPOSITORY}:${IMAGE_TAG}")
                    // echo 'AWS Docker build succeeded'
                    withAWS(credentials: 'AWS-Jenkins1', region: "${AWS_DEFAULT_REGION}") {
                        bat "aws ecr get-login-password --region ${AWS_DEFAULT_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com"

                        bat "docker tag nchinling/jenkins_lovecalc_repo:latest ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${ECR_REPOSITORY}:${IMAGE_TAG}"

                        // bat "docker tag ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${ECR_REPOSITORY}:${IMAGE_TAG} ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${ECR_REPOSITORY}:${IMAGE_TAG}"

                        bat "docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${ECR_REPOSITORY}:${IMAGE_TAG}"
                    }

                    // Zip the Dockerrun.aws.json for Elastic Beanstalk deployment
                    // bat '7z a deployment-package.zip Dockerrun.aws.json'
                    bat 'powershell -Command "Compress-Archive -Path Dockerrun.aws.json -DestinationPath deployment-package.zip"'
                    echo 'zip completed '
                    // Create a new application version and update the environment
                    withAWS(credentials: 'AWS-Jenkins1', region: "${AWS_DEFAULT_REGION}") {
                        bat "aws s3 cp deployment-package.zip s3://${S3_BUCKET}/${EB_APPLICATION_NAME}-${IMAGE_TAG}.zip"
                        bat "aws elasticbeanstalk create-application-version --application-name ${EB_APPLICATION_NAME} --version-label ${IMAGE_TAG} --source-bundle S3Bucket=${S3_BUCKET},S3Key=${EB_APPLICATION_NAME}-${IMAGE_TAG}.zip"
                        bat "aws elasticbeanstalk update-environment --application-name ${EB_APPLICATION_NAME} --environment-name ${EB_ENVIRONMENT_NAME} --version-label ${IMAGE_TAG}"
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'All stages passed successfully.'
        }
        failure {
            script {
                mail bcc: '', body: 'Hello, This is an email from Jenkins pipeline. Build or containerisation has failed. Please check the logs for details.', cc: '', from: '', replyTo: '', subject: 'Build / Containerisation Failed', to: 'nchinling@gmail.com'
            }
        }
    }
}
