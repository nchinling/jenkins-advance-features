/* groovylint-disable DuplicateStringLiteral, LineLength, NestedBlockDepth */
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
        AWS_DEFAULT_REGION = 'ap-southeast-1'
        AWS_ACCOUNT_ID = '851725323495'
        ECR_REPOSITORY = 'love-calculator'
        IMAGE_TAG = "latest-${env.BUILD_ID}"
        VERSION_LABEL = "latest-${env.BUILD_ID}"
        EB_APPLICATION_NAME = 'love-calculator'
        EB_ENVIRONMENT_NAME = 'Love-calculator-env'
        S3_BUCKET = 'elasticbeanstalk-ap-southeast-1-851725323495'
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
        stage('Sonar') {
            steps {
                echo 'Start Sonar'
                //static code analysis and skipping tests which was previously run
                bat 'mvn sonar:sonar -DskipTests'
                echo 'Sonar completed'
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
                            echo 'Building new Docker image'
                            bat "docker build -t nchinling/jenkins_lovecalc_repo:${IMAGE_TAG} ."

                            echo 'Pushing Docker image'
                            withCredentials([usernamePassword(credentialsId: 'nchinling-dockerhub', passwordVariable: 'dockerHubPassword', usernameVariable: 'dockerHubUser')]) {
                                bat "docker login -u ${env.dockerHubUser} -p ${env.dockerHubPassword}"
                                bat "docker push nchinling/jenkins_lovecalc_repo:${IMAGE_TAG}"
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
                echo 'Push Docker image to ECR'
                script {
                    withAWS(credentials: 'AWS-Jenkins1', region: "${AWS_DEFAULT_REGION}") {
                        bat 'aws ecr get-login-password | docker login --username AWS --password-stdin 851725323495.dkr.ecr.ap-southeast-1.amazonaws.com/love-calculator'
                        bat "docker tag nchinling/jenkins_lovecalc_repo:${IMAGE_TAG} 851725323495.dkr.ecr.ap-southeast-1.amazonaws.com/love-calculator:latest"
                        bat 'docker push 851725323495.dkr.ecr.ap-southeast-1.amazonaws.com/love-calculator:latest'
                        }
                    // Zip the Dockerrun.aws.json for Elastic Beanstalk deployment
                    bat 'powershell -Command "Compress-Archive -Path Dockerrun.aws.json -DestinationPath deployment-package.zip -Force"'
                    echo 'Zip completed'

                    // Create a new application version
                    withAWS(credentials: 'AWS-Jenkins1', region: "${AWS_DEFAULT_REGION}") {
                        bat "aws s3 cp deployment-package.zip s3://${S3_BUCKET}/${EB_APPLICATION_NAME}-${VERSION_LABEL}.zip"
                        bat "aws elasticbeanstalk create-application-version --application-name ${EB_APPLICATION_NAME} --version-label ${VERSION_LABEL} --source-bundle S3Bucket=${S3_BUCKET},S3Key=${EB_APPLICATION_NAME}-${VERSION_LABEL}.zip"

                        // Update Elastic Beanstalk environment with the new version
                        bat "aws elasticbeanstalk update-environment --application-name ${EB_APPLICATION_NAME} --environment-name ${EB_ENVIRONMENT_NAME} --version-label ${VERSION_LABEL}"
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
