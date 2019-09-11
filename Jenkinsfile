
timestamps
{
	//noinspection GroovyAssignabilityCheck
	lock('sendmail-remote') { node('OpenJdk18Debian9')
	{
		try
		{
			abortable
			{
				echo("Delete working dir before build")
				deleteDir()

				def scmResult = checkout scm
				computeGitTree(scmResult)

				env.BUILD_TIMESTAMP = new Date().format("yyyy-MM-dd_HH-mm-ss");
				env.JAVA_HOME = "${tool 'openjdk 1.8 debian9'}"
				env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
				def antHome = tool 'Ant version 1.9.3'

				sh "java -version"
				sh "${antHome}/bin/ant -version"

				def isRelease = env.BRANCH_NAME.toString().equals("master");

				properties([
						buildDiscarder(logRotator(
								numToKeepStr         : isRelease ? '1000' : '15',
								artifactNumToKeepStr : isRelease ? '1000' :  '2'
						))
				])

				sh 'echo' +
						' scmResult=' + scmResult +
						' BUILD_TIMESTAMP -${BUILD_TIMESTAMP}-' +
						' BRANCH_NAME -${BRANCH_NAME}-' +
						' BUILD_NUMBER -${BUILD_NUMBER}-' +
						' BUILD_ID -${BUILD_ID}-' +
						' isRelease=' + isRelease

				sh "${antHome}/bin/ant clean jenkins" +
						' "-Dbuild.revision=${BUILD_NUMBER}"' +
						' "-Dbuild.tag=git ${BRANCH_NAME} ' + scmResult.GIT_COMMIT + ' ' + scmResult.GIT_TREE + ' jenkins ${BUILD_NUMBER} ${BUILD_TIMESTAMP}"' +
						' -Dbuild.status=' + (isRelease?'release':'integration') +
						' -DskipRemote=true' +
						' -Dfindbugs.output=xml'

				warnings(
						canComputeNew: true,
						canResolveRelativePaths: true,
						categoriesPattern: '',
						consoleParsers: [[parserName: 'Java Compiler (javac)']],
						defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '', messagesPattern: '', unHealthy: '',
						unstableTotalAll: '0',
						usePreviousBuildAsReference: false,
						useStableBuildAsReference: false,
				)
				findbugs(
						canComputeNew: true,
						defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '',
						isRankActivated: false,
						pattern: 'build/findbugs.xml',
						unHealthy: '',
						unstableTotalAll: '0',
						usePreviousBuildAsReference: false,
						useStableBuildAsReference: false,
				)
				withCredentials([file(credentialsId: 'sendmail-remote.properties', variable: 'PROPERTIES')])
				{
					sh "${antHome}/bin/ant test -propertyfile " + PROPERTIES + " -Dtest-taskname=junit-plain    -Dsmtp.port=25"
					sh "${antHome}/bin/ant test -propertyfile " + PROPERTIES + " -Dtest-taskname=junit-ssltls   -Dsmtp.port=465 -Dsmtp.ssl=true"
					sh "${antHome}/bin/ant test -propertyfile " + PROPERTIES + " -Dtest-taskname=junit-starttls -Dsmtp.port=587 -Dsmtp.enableStarttls=true"
					sh "${antHome}/bin/ant test -propertyfile " + PROPERTIES + " -Dtest-taskname=junit-start25  -Dsmtp.port=25  -Dsmtp.enableStarttls=true"
				}
				archive 'build/success/*'
			}
		}
		catch(Exception e)
		{
			//todo handle script returned exit code 143
			throw e;
		}
		finally
		{
			// because junit failure aborts ant
			junit(
					allowEmptyResults: false,
					testResults: 'build/testresults/*.xml',
			)
			def to = emailextrecipients([
					[$class: 'CulpritsRecipientProvider'],
					[$class: 'RequesterRecipientProvider']
			])
			//TODO details
			step([$class: 'Mailer',
					recipients: to,
					attachLog: true,
					notifyEveryUnstableBuild: true])

			if('SUCCESS'.equals(currentBuild.result) ||
				'UNSTABLE'.equals(currentBuild.result))
			{
				echo("Delete working dir after " + currentBuild.result)
				deleteDir()
			}
		}
	}}
}

def abortable(Closure body)
{
	try
	{
		body.call();
	}
	catch(hudson.AbortException e)
	{
		if(e.getMessage().contains("exit code 143"))
			return
		throw e;
	}
}

def computeGitTree(scmResult)
{
	sh "git cat-file -p " + scmResult.GIT_COMMIT + " | grep '^tree ' | sed -e 's/^tree //' > .git/jenkins-head-tree"
	scmResult.GIT_TREE = readFile('.git/jenkins-head-tree').trim()
}
