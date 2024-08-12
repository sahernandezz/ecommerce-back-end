pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // Clonar el repositorio
                git 'https://github.com/sahernandezz/ecommerce-back-end.git'
            }
        }

        stage('Build') {
            steps {
                // Dar permisos de ejecución a gradlew
                sh 'chmod +x ./gradlew'
                // Construir el proyecto con Gradle
                sh './gradlew clean build'
            }
        }

        stage('Deploy') {
            steps {
                // Desplegar usando Docker Compose
                sh 'docker-compose up -d --build'
            }
        }
    }

    post {
        success {
            // Enviar notificación de éxito
            echo 'Pipeline completado con éxito!'
        }
        failure {
            // Enviar notificación de fallo
            echo 'Pipeline fallido'
        }
    }
}