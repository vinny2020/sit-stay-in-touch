import SwiftUI
import SwiftData
import UniformTypeIdentifiers

struct ImportView: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @AppStorage("hasCompletedOnboarding") private var hasCompletedOnboarding = false

    @State private var isImporting = false
    @State private var showingDocumentPicker = false
    @State private var importError: String?
    @State private var showingError = false

    var body: some View {
        NavigationStack {
            List {
                Section {
                    Button {
                        Task { await importFromiOS() }
                    } label: {
                        HStack {
                            Label("Import from iPhone Contacts", systemImage: "apple.logo")
                            Spacer()
                            if isImporting {
                                ProgressView()
                            }
                        }
                    }
                    .disabled(isImporting)

                    Button {
                        showingDocumentPicker = true
                    } label: {
                        Label("Import from LinkedIn CSV", systemImage: "briefcase.fill")
                    }
                    .disabled(isImporting)
                } footer: {
                    Text("LinkedIn usually emails your download link within 10–30 minutes. Come back here once you have the CSV.")
                }

                Section("How to get your LinkedIn CSV") {
                    VStack(alignment: .leading, spacing: 10) {
                        LinkedInStep(number: "1", text: "Open linkedin.com in Safari (works on iPhone)")
                        LinkedInStep(number: "2", text: "Tap your photo → Settings & Privacy")
                        LinkedInStep(number: "3", text: "Data Privacy → Get a copy of your data")
                        LinkedInStep(number: "4", text: "Select Connections only → Request archive")
                        LinkedInStep(number: "5", text: "Wait for LinkedIn's email (10–30 min)")
                        LinkedInStep(number: "6", text: "Download the zip → open in Files app → tap to unzip")
                        LinkedInStep(number: "7", text: "Come back here and tap Import from LinkedIn CSV")
                    }
                    .padding(.vertical, 4)
                }
            }
            .navigationTitle("Import Contacts")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
            .fileImporter(
                isPresented: $showingDocumentPicker,
                allowedContentTypes: [.commaSeparatedText, .text],
                allowsMultipleSelection: false
            ) { result in
                switch result {
                case .success(let urls):
                    guard let url = urls.first else { return }
                    importFromLinkedIn(url: url)
                case .failure(let error):
                    importError = error.localizedDescription
                    showingError = true
                }
            }
            .alert("Import Error", isPresented: $showingError) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(importError ?? "Unknown error")
            }
        }
    }

    @MainActor
    private func importFromiOS() async {
        isImporting = true
        defer { isImporting = false }
        do {
            try await ContactImportService.importFromiOS(context: modelContext)
            hasCompletedOnboarding = true
            dismiss()
        } catch {
            importError = error.localizedDescription
            showingError = true
        }
    }

    private func importFromLinkedIn(url: URL) {

        let accessed = url.startAccessingSecurityScopedResource()
        defer { if accessed { url.stopAccessingSecurityScopedResource() } }
        do {
            try LinkedInCSVParser.parse(url: url, context: modelContext)
            hasCompletedOnboarding = true
            dismiss()
        } catch {
            importError = error.localizedDescription
            showingError = true
        }
    }
}

private struct LinkedInStep: View {
    let number: String
    let text: String

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Text(number)
                .font(.caption)
                .fontWeight(.semibold)
                .foregroundStyle(.white)
                .frame(width: 20, height: 20)
                .background(Color.indigo)
                .clipShape(Circle())
            Text(text)
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .fixedSize(horizontal: false, vertical: true)
        }
    }
}
