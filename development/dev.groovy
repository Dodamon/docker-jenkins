pipeline {
  agent any
  stages {
    stage('clone git lab repository') {
      steps {
        git branch: 'dev', credentialsId: 'gitlab', url: 'https://lab.ssafy.com/s08-bigdata-recom-sub2/S08P22A504.git'
      }
    }
    stage('client, server parallel build') {
      parallel {
        stage('client build') {
          steps {
            script {
              try {
                echo 'build client'
                sh 'docker build -f frontend/Dockerfile.dev -t nowgnas/osakak:devclient .'
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
        stage('server build') {
          steps {
            script {
              try {
                echo 'build server'
                sh 'docker build -f backend/Dockerfile.dev -t nowgnas/osakak:devserver .'
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
        sh 'docker push nowgnas/osakak:devserver'
        sh 'docker push nowgnas/osakak:devclient'
      }
    }
    stage('stop dev server') {
      steps {
        script {
          try {
            sh 'docker rm -f devclient devserver'
          } catch(e) {
            echo 'no running development server'
          }
        }
      }
    }
    stage('run dev server') {
      steps {
        script {
          try {
            echo 'start dev server'
            sh 'docker login -u nowgnas -p dltkddnjs!!'
            sh 'cd /home/ubuntu/development && docker-compose -f compose-dev.yml up -d'
          } catch (e) {
            echo 'development server run fail'
            mattermostSend(
              color: "#DF2E38",
              message: "[RUN DEV SERVER FAIL]: ${env.JOB_NAME} | #${env.BUILD_NUMBER} | URL: ${env.BUILD_URL} link to build"
            )
          }
        }
      }
    }
    stage('create merge request to release') {
      steps {
        script {
          try {
            sh 'curl -X POST -H "PRIVATE-TOKEN: 22HMryj9spaoUeCQ5sjM" -H "Content-Type: application/json" -d \'{"id": "287656", "source_branch": "dev", "target_branch": "release", "title": ":twisted_rightwards_arrows: create merge request dev into release"}\' https://lab.ssafy.com/api/v4/projects/287656/merge_requests'
          } catch (e) {
            echo "create merge request fail"
            mattermostSend(
              color: "#DF2E38",
              message: "[CREATE MERGE REQUEST FAIL]: ${env.JOB_NAME} | #${env.BUILD_NUMBER} | URL: ${env.BUILD_URL} link to build"
            )
          }
        }
      }
    }
    stage('remove unused images') {
      steps {
        script {
          try {
            sh 'docker images -qf dangling=true | xargs -I{} docker rmi {}'
        } catch (e) {
            echo 'no unused images'
          }
        }
      }
    }
    stage('dev jenkins job finish') {
      steps {
        mattermostSend(
          color: "good",
          message: "[DEV JENKINS JOB SUCCESS]: ${env.JOB_NAME} | #${env.BUILD_NUMBER} | URL: ${env.BUILD_URL} link to build"
        )
      }
    }
  }
}