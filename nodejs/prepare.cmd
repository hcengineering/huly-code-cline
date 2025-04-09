set CLINE_COMMIT=v3.9.2

git clone --depth 1 --branch %CLINE_COMMIT% https://github.com/cline/cline.git cline
rmdir /s cline\.git
