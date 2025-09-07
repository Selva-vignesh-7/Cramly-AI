document.addEventListener('DOMContentLoaded', () => {
    chrome.storage.local.get(['researchNotes'], function(result) {
        if(result.researchNotes) {
            document.getElementById('notes').value = result.researchNotes;
        }
    });

    document.getElementById('summarizeBtn').addEventListener('click', summarizeText);
    document.getElementById('saveNotesBtn').addEventListener('click', saveNotes);

});


async function summarizeText() {
    try {
        document.getElementById('loader').style.display = 'block'; // show loader

        const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
        const [{ result }] = await chrome.scripting.executeScript({
            target: { tabId: tab.id },
            function: () => window.getSelection().toString()
        });

        if (!result) {
            showResult("Please select some text on the page to summarize...");
            document.getElementById('loader').style.display = 'none'; // hide loader
            return;
        }



        

        const API_URL = "https://demo-deployement-latest-1.onrender.com"; // Ensure this matches your config.js
        const response = await fetch(`${API_URL}/api/research/process`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content: result, operation: 'summarize' })
        });

        if (!response.ok) {
            throw new Error(`API Error: ${response.status}`);
        }

        const text = await response.text();
        showResult(text.replace(/\\n/g, '<br>'));
    } catch (error) {
        showResult("Error: " + error.message);
    } finally {
        document.getElementById('loader').style.display = 'none'; // always hide loader
    }
}


async function saveNotes(params) {
    const notes = document.getElementById('notes').value;
    chrome.storage.local.set({ researchNotes: notes }, function() {
        alert('Roger That !');
    });
}

function showResult(content) {
    document.getElementById('result').innerHTML = `<div class="result-item"><div class="result-content">${content}</div></div>`;
}