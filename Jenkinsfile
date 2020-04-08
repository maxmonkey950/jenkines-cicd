def label = "slave-${UUID.randomUUID().toString()}"

podTemplate(label: label, containers: [
  containerTemplate(name: 'maven', image: 'maven:3.6-alpine', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'kubectl', image: 'cnych/kubectl', command: 'cat', ttyEnabled: true)
], volumes: [
  //persistentVolumeClaim(claimName: 'opspvc-ha-mvn', mountPath: '/root/.m2'),
  hostPathVolume(mountPath: '/root/.m2', hostPath: '/var/run/m2'),
  hostPathVolume(mountPath: '/home/jenkins/.kube', hostPath: '/root/.kube'),
  hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')
]) {
  node(label) {
    def myRepo = checkout scm
    def gitCommit = myRepo.GIT_COMMIT
    def gitBranch = myRepo.GIT_BRANCH
    def imageTag = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
    def imageTag2 = sh(script: "date +%Y%m%d%H%M%S", returnStdout: true).trim()
    def imageTag3 = "${imageTag2}-${imageTag}"
    def dockerRegistryUrl = "registry.cn-shenzhen.aliyuncs.com"
    def imageEndpoint = "xxx/devops-test"
    def image = "${dockerRegistryUrl}/${imageEndpoint}"
    if ("${gitBranch}" == 'master') {
                imageTag = "${gitBranch}-${imageTag}"
            }
    stage('单元测试') {
      echo "1.测试阶段"
      sh 'printenv'
    }
    stage('代码编译打包') {
      try {
        container('maven') {
          echo "2. 代码编译打包阶段"
          sh "mvn clean package -Dmaven.test.skip=true"
        }
      } catch (exc) {
        println "构建失败 - ${currentBuild.fullDisplayName}"
        throw(exc)
      }
    }
    container('构建 Docker 镜像') {
      withCredentials([[$class: 'UsernamePasswordMultiBinding',
        credentialsId: 'alihub',
        usernameVariable: 'alihubUser',
        passwordVariable: 'alihubPassword']]) {
          container('docker') {
            echo "3. 构建 Docker 镜像阶段"
            sh """
              docker login ${dockerRegistryUrl} -u ${alihubUser} -p ${alihubPassword}
              docker build -t ${image}:${imageTag} .
              docker push ${image}:${imageTag}
              """
          }
       }
    }
    
    stage('上传镜像') {
      container('kubectl') {
        echo "查看 K8S 集群 Pod 列表"
        sh "kubectl get pods"
      }
    }
    stage('部署') {
      container('kubectl') {
        echo "5. Deploy Stage"
        if ("${gitBranch}" == 'master') {
            input "确认要部署线上环境吗？"
        }
        sh """
          sed -i "s#<IMAGE>#${image}#g" manifests/k8s.yaml
          sed -i "s#<IMAGE_TAG>#${imageTag}#g" manifests/k8s.yaml
          kubectl apply -f manifests/k8s.yaml --record
        """
      }
    }
  }
}
