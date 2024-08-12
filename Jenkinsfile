pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // Clonar el repositorio
                git 'https://github.com/tu-usuario/tu-repositorio.git'
            }
        }

        stage('Build') {
            steps {
                // Construir el proyecto con Gradle
                sh './gradlew clean build'
            }
        }

        stage('Test') {
            steps {
                // Ejecutar pruebas con Gradle
                sh './gradlew test'
            }
            post {
                always {
                    // Archivar resultados de pruebas
                    junit '**/build/test-results/test/*.xml'
                }
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