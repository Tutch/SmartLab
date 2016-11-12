#include "stdafx.h"
#include "Defines.h"
#include <windows.h>
#include <time.h>
#include <iostream>

HHOOK mouseHook;
HHOOK keyboardHook;
clock_t start_time, current_time;
bool monitor_is_on = true;
bool is_shutting_down = false;

/* Chama o .bat que faz o desligamento da máquina
*/
VOID executeCommand(wchar_t* wBatPath)
{
	// additional information
	STARTUPINFO si;
	PROCESS_INFORMATION pi;

	// set the size of the structures
	ZeroMemory(&si, sizeof(si));
	si.cb = sizeof(si);
	ZeroMemory(&pi, sizeof(pi));

	wchar_t cmd[120] = L"cmd.exe /C ";
	wcsncpy_s(cmd, wBatPath, wcslen(cmd) - 12);

	// start the program up
	if (!CreateProcess(NULL,   // the path
		cmd,        // Command line
		NULL,           // Process handle not inheritable
		NULL,           // Thread handle not inheritable
		FALSE,          // Set handle inheritance to FALSE
		0,              // No creation flags
		NULL,           // Use parent's environment block
		NULL,           // Use parent's starting directory 
		&si,            // Pointer to STARTUPINFO structure
		&pi)           // Pointer to PROCESS_INFORMATION structure
		) {
		int error = GetLastError();
		wchar_t buffer[256];
		wsprintfW(buffer, L"%d", error);
		OutputDebugString(buffer);
		abort();
	}

	// Close process and thread handles. 
	CloseHandle(pi.hProcess);
	CloseHandle(pi.hThread);
}

/* http://stackoverflow.com/questions/15968520/confusion-about-mouse-hooks-in-c
 * Called on mouse & keyboard events
 * Sets the variable responsible for the starting time to the current time,
 * essentialy reseting the counter.
 */
LRESULT CALLBACK inputHookProc(int nCode, WPARAM wParam, LPARAM lParam)
{
	PMSLLHOOKSTRUCT p = (PMSLLHOOKSTRUCT)lParam;
	#ifdef DEBUG
	printf("Evento mouse/teclado");
	#endif // DEBUG

	start_time = clock();
	monitor_is_on = true;
	
	return CallNextHookEx(NULL, nCode, wParam, lParam);
}

/* http://stackoverflow.com/questions/11180773/setwindowshookex-for-wh-mouse
 * Sets hooks for mouse & keyboard.
 */
DWORD WINAPI inputLogger(LPVOID lpParm)
{
	HINSTANCE hInstance = GetModuleHandle(NULL);

	mouseHook = SetWindowsHookEx(WH_MOUSE_LL, inputHookProc, hInstance, NULL);
	keyboardHook = SetWindowsHookEx(WH_KEYBOARD_LL, inputHookProc, hInstance, NULL);


	MSG message;
	while (GetMessage(&message, NULL, 0, 0)) {
		TranslateMessage(&message);
		DispatchMessage(&message);
	}

	UnhookWindowsHookEx(mouseHook);
	return 0;
}


int APIENTRY wWinMain(_In_ HINSTANCE hInstance,
                     _In_opt_ HINSTANCE hPrevInstance,
                     _In_ LPWSTR    lpCmdLine,
                     _In_ int       nCmdShow)
{
	HANDLE hThread;
	DWORD dwThread;
	clock_t time_diff;
	start_time = clock();


	// Tries to create a new thread to handle the hook events
	hThread = CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)inputLogger, NULL, 0, &dwThread);

	// If the handle is kaput, interrupt the program
	if (!hThread) {
		return 1;
	}

	/* MAIN LOOP
	* 1. Calculate the difference between starting time and right now
	* 2. Compare to the defined values on Define.h to check if any action should be taken
	* 3. Act on it
	*/
	while (true) {
		Sleep(1000);
		
		current_time = clock();
		time_diff = current_time - start_time;

		// Display off
		if (time_diff >= DISPLAY_TURN_OFF_TIME && monitor_is_on) {
			monitor_is_on = false;
			OutputDebugString(L"display\n");
			PostMessage(HWND_BROADCAST, WM_SYSCOMMAND, SC_MONITORPOWER, (LPARAM)2);
		}

		// Shutdown Computer
		else if (time_diff >= SYSTEM_SHUT_DOWN_TIME && !monitor_is_on && !is_shutting_down) {
			is_shutting_down = true;
			OutputDebugString(L"calling bat\n");
			executeCommand(SHUTDOWN_PATH);
		}

	}

	WaitForSingleObject(hThread, INFINITE);
	CloseHandle(hThread);

	return 0;
}