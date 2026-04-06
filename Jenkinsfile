pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
    }

    tools {
        jdk 'Java'
        maven 'Maven'
    }

    environment {
        REPO_URL = 'git@github.com:stha-sush/library-room-booking.git'
        APP_NAME = 'library-room-booking-0.0.1-SNAPSHOT.jar'
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'dev',
                    credentialsId: 'git',
                    url: "${REPO_URL}"
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn clean verify sonar:sonar \
                          -Dsonar.projectKey=my-project
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Deploy Dev') {
            steps {
                sh '''
                    mkdir -p ~/deployment/dev
                    cp target/${APP_NAME} ~/deployment/dev
                    cd ~/deployment/dev
                    pkill -f ${APP_NAME} || true
                    nohup java -jar ${APP_NAME} --server.port=8081 > app.log 2>&1 &
                '''
            }
        }
    }
}