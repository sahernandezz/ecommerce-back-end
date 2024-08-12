pipeline {
    agent any

    triggers {
        pollSCM('H/10 * * * *')
    }

    environment {
        DOCKER_HOST = 'tcp://192.168.0.24:2375'
    }

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/sahernandezz/ecommerce-back-end.git'
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew clean build'
            }
        }

        stage('Deploy') {
            steps {
                sh 'docker-compose up -d --build'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completado con éxito!'
        }
        failure {
            echo 'Pipeline fallido'
        }
    }
}
