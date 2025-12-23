describe('Flow de Agendamento Público', () => {
    // 1. Visitar a página com um código válido (precisamos de um user existente ou mockar)
    // Para simplificar, vamos assumir que o profissional 'ana' existe ou usar a rota publica direta se soubermos o ID
    // Mas o ideal é testar o fluxo real.
    // Vamos visitar a página do profissional slug 'test-cypress' (precisaremos criar esse user no backend test)
    // OU, como é E2E, podemos assumir que o backend está populado ou usar um user conhecido.
    // Vou usar um slug genérico que o backend deve tratar, ou melhor, mockar a resposta da API.

    beforeEach(() => {
        // Mock da API para não depender de dados reais do banco
        cy.intercept('GET', '**/api/public/profissional/slug/*', (req) => {
            req.reply({
                statusCode: 200,
                body: {
                    id: 999,
                    nome: 'Profissional Teste',
                    email: 'teste@space.nails',
                    codigoConvite: 'TEST99'
                }
            })
        }).as('getProfissional')

        cy.intercept('GET', '**/api/public/profissional/999/info', {
            statusCode: 200,
            body: {
                nome: 'Profissional Teste',
                telefone: '5511999999999',
                endereco: 'Rua dos Testes, 123',
                localizacaoUrl: 'http://maps.google.com'
            }
        }).as('getInfo')

        cy.intercept('GET', '**/api/public/servicos?profissionalId=999', {
            statusCode: 200,
            body: [
                { id: 101, nome: 'Manicure Gel', valor: 80.0, tempoEstimado: 60, profissional: { id: 999 } }
            ]
        }).as('getServicos')

        cy.visit('/agendar/TEST99')
    })

    it('Deve carregar a página do profissional e listar serviços', () => {
        cy.wait('@getProfissional')
        cy.contains('Space.Nails')
        cy.contains('Profissional Teste')

        // Step 0 -> Novo Agendamento
        cy.contains('Novo Agendamento').click()

        // Step 1 -> Escolha de Serviço
        cy.wait('@getServicos')
        cy.contains('Manicure Gel').should('be.visible')
        cy.contains('R$ 80').should('be.visible')
    })

    it('Deve realizar um agendamento completo', () => {
        // Mock dos Slots
        const hoje = new Date().toISOString().split('T')[0]
        cy.intercept('GET', `**/api/public/slots*`, {
            statusCode: 200,
            body: ['10:00', '14:00', '16:00']
        }).as('getSlots')

        // Mock do Post Agendar
        cy.intercept('POST', '**/api/public/agendar', {
            statusCode: 200,
            body: {
                id: 500,
                codigo: 'CODE123',
                status: 'PENDENTE',
                dataHora: `${hoje}T10:00:00`,
                cliente: { nome: 'Cliente Cypress', telefone: '11999998888' },
                servico: { nome: 'Manicure Gel', valor: 80.0 },
                profissional: { nome: 'Profissional Teste' }
            }
        }).as('postAgendar')

        // 0. Inicio
        cy.contains('Novo Agendamento').click()

        // 1. Serviço
        cy.contains('Manicure Gel').click()

        // 2. Data e Hora
        // Força a seleção da data de hoje no input date
        cy.get('input[type="date"]').type(hoje).trigger('change').trigger('input')
        cy.wait('@getSlots')
        cy.contains('10:00').click()
        cy.contains('Próximo').click()

        // 3. Identificação
        cy.get('input[placeholder="(00) 00000-0000"]').type('11999998888')
        cy.get('input[placeholder="Seu nome"]').type('Cliente Cypress')
        cy.contains('Confirmar Agendamento').click()

        // 4. Sucesso
        cy.wait('@postAgendar')
        cy.contains('Agendado!').should('be.visible')
        cy.contains('CODE123').should('be.visible')
    })
})
