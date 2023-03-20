pipeline {
  agent any
  stages {
    stage('clone git lab repository') {
      steps {
        git branch: 'main', credentialsId: 'gitlab', url: 'https://lab.ssafy.com/s08-bigdata-recom-sub2/S08P22A504.git'
      }
    }
    stage('client, server parallel build') {
      parallel {
        stage('production client build') {
          steps {
            script {
              try {
                echo 'build client'
                sh 'docker build -f frontend/Dockerfile -t nowgnas/osakak:client .'
              } catch (e) {
                echo 'client build fail'
                mattermostSend(
                  color: "#DF2E38",
                  message: "[CLIENT BUILD FAIL]: ${env.JOB_NAME} | #${env.BUILD_NUMBER} | URL: ${env.BUILD_URL} link to build"
                )
              }
            }
            
          }
        }
        stage('staging server build') {
          steps {
            script {
              try {
                echo 'build server'
                sh 'docker build -f backend/Dockerfile.prod -t nowgnas/osakak:server .'
              } catch (e) {
                echo 'server build fail'
                mattermostSend(
                  color: "#DF2E38",
                  message: "[SERVER BUILD FAIL]: ${env.JOB_NAME} | #${env.BUILD_NUMBER} | URL: ${env.BUILD_URL} link to build"
                )
              }
            }
          }
        }
      }
    }
    stage('push build images') {
      steps {
        echo 'build image'
        sh 'docker login -u nowgnas -p dltkddnjs!!'
        sh 'docker push nowgnas/osakak:server'
        sh 'docker push nowgnas/osakak:client'
      }
    }
    stage('stop main server') {
      steps {
        script {
          try {
            sh 'docker rm -f client server'
          } catch(e) {
            echo 'no running main server'
          }
        }
      }
    }
    stage('run staging server') {
      steps {
        script {
          try {
            echo 'start main server'
            sh 'docker login -u nowgnas -p dltkddnjs!!'
            sh 'cd /home/ubuntu/deploy && docker-compose up -d'
          } catch (e) {
            echo 'production server run fail'
            mattermostSend(
              color: "#DF2E38",
              message: "[RUN PRODUCTION SERVER FAIL]: ${env.JOB_NAME} | #${env.BUILD_NUMBER} | URL: ${env.BUILD_URL} link to build"
            )
          }
        }
      }
    }
    stage('release jenkins job finish') {
      steps {
        mattermostSend(
          color: "good",
          message: "[PRODUCTION JENKINS JOB SUCCESS]: ${env.JOB_NAME} | #${env.BUILD_NUMBER} | URL: ${env.BUILD_URL} link to build"
        )
      }
    }
  }
}