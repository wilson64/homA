

npm_update: 
	find . -maxdepth 3 -type f -name "package.json" -print0 | xargs -0 -IX sh -c 'cd `dirname X` && echo Updating node modules in X && npm update'
